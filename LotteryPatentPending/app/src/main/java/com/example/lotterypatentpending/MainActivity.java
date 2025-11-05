package com.example.lotterypatentpending;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;


import com.example.lotterypatentpending.models.FirebaseManager;
import com.example.lotterypatentpending.models.User;
import com.example.lotterypatentpending.models.UserEventRepository;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;

/**
 * Class MainActivity
 * @maintainer Erik
 * @author Erik
 */


public class MainActivity extends AppCompatActivity implements MainRegisterNewUserFragment.OnProfileSaved {
    private UserEventRepository userEventRepo;
    private FirebaseManager fm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // get user event repo instance
        userEventRepo = UserEventRepository.getInstance();

        // Buttons
        findViewById(R.id.btnAttendee).setOnClickListener(v ->
                startActivity(new Intent(this, AttendeeActivity.class)));
        findViewById(R.id.btnOrganizer).setOnClickListener(v ->
                startActivity(new Intent(this, OrganizerActivity.class)));
        findViewById(R.id.btnAdmin).setOnClickListener(v ->
                startActivity(new Intent(this, AdminActivity.class)));

        FirebaseApp.initializeApp(this);
        fm = FirebaseManager.getInstance();

        // Sign in user to firebase anonymously
        // get the uid from firebase
        FirebaseAuth.getInstance().signInAnonymously()
                .addOnSuccessListener(r -> {
                    String uid = r.getUser().getUid();
                    checkUserDoc(uid);
                })
                .addOnFailureListener(e -> {
                    registerNewUserOverlay();
                });

        // TODO: get user info
        User user = new User();
        userEventRepo.setUser(user);
    }

    private void checkUserDoc(String uid) {
        fm.getUser(uid, new FirebaseManager.FirebaseCallback<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot snap) {
                if (!snap.exists()) {
                    registerNewUserOverlay();
                }
                else {
                    //User already exists gets its name
                    User user = snap.toObject(User.class);
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
                    .commit();
        }
    }

    @Override
    public void onProfileSaved() {
        int containerId = R.id.createUserOverlay;
        FragmentManager fm = getSupportFragmentManager();
        if (fm.findFragmentById(containerId) != null) {
            fm.beginTransaction().remove(fm.findFragmentById(containerId)).commit();
        }
        View container = findViewById(containerId);
        if (container != null) container.setVisibility(View.GONE);
    }
}