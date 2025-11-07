package com.example.lotterypatentpending;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;


import com.example.lotterypatentpending.models.FirebaseManager;
import com.example.lotterypatentpending.models.User;
import com.example.lotterypatentpending.viewModels.UserEventRepository;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Class MainActivity
 * @collaborators Erik, Michael
 */
public class MainActivity extends AppCompatActivity implements MainRegisterNewUserFragment.OnProfileSaved {
    private FirebaseManager fm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);
        fm = FirebaseManager.getInstance();

        findViewById(R.id.main_button_attendee).setOnClickListener(v ->
                startActivity(new Intent(this, AttendeeActivity.class)));
        findViewById(R.id.main_button_organizer).setOnClickListener(v ->
                startActivity(new Intent(this, OrganizerActivity.class)));
        findViewById(R.id.main_button_admin).setOnClickListener(v ->
                startActivity(new Intent(this, AdminActivity.class)));

        FirebaseAuth auth = FirebaseAuth.getInstance();

        // if user already signed in
        if (auth.getCurrentUser() != null) {
            String uid = auth.getCurrentUser().getUid();
            checkUserDoc(uid);
        }
        else {
            auth.signInAnonymously()
                    .addOnSuccessListener(r -> {
                        String uid = r.getUser().getUid();
                        checkUserDoc(uid);
                    })
                    .addOnFailureListener(e -> {
                        registerNewUserOverlay();
                    });
        }

    }

    private void checkUserDoc(String uid) {
        fm.getUser(uid, new FirebaseManager.FirebaseCallback<User>() {
            @Override
            public void onSuccess(User user) {
                if (user == null) {
                    registerNewUserOverlay();
                }
                else {
                    UserEventRepository.getInstance().setUser(user);
                }
            }
            @Override
            public void onFailure(Exception e) {
                // if something goes wrong, just onboard
                registerNewUserOverlay();
            }
        });
    }

    private void registerNewUserOverlay() {
        int containerId = R.id.createUserOverlay; // make sure this exists in activity_main.xml
        View container = findViewById(containerId);
        if (container == null) return;

        container.setVisibility(View.VISIBLE);

        FragmentManager fm = getSupportFragmentManager();
        if (fm.findFragmentById(containerId) == null) {
            fm.beginTransaction()
                    .replace(containerId, new MainRegisterNewUserFragment())
                    .commitAllowingStateLoss();
        }
    }

    @Override
    public void onProfileSaved() {
        int containerId = R.id.createUserOverlay;
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(containerId);

        if (fragment != null) {
            fm.beginTransaction().remove(fragment).commit();
        }

        View container = findViewById(containerId);
        if (container != null) container.setVisibility(View.GONE);

        // reload user
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            checkUserDoc(firebaseUser.getUid());
        }
    }
}