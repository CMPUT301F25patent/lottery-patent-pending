package com.example.lotterypatentpending;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

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
    private View attendeeBtn;
    private View organizerBtn;
    private View adminBtn;
    private View loadingOverlay;
    private View mainLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);
        fm = FirebaseManager.getInstance();

        attendeeBtn = findViewById(R.id.main_button_attendee);
        organizerBtn = findViewById(R.id.main_button_organizer);
        adminBtn = findViewById(R.id.main_button_admin);
        loadingOverlay = findViewById(R.id.loading_overlay);
        mainLayout = findViewById(R.id.main_layout);

        if (mainLayout != null) mainLayout.setVisibility(View.GONE);




        // click listeners stay the same
        attendeeBtn.setOnClickListener(v ->
                startActivity(new Intent(this, AttendeeActivity.class)));
        organizerBtn.setOnClickListener(v ->
                startActivity(new Intent(this, OrganizerActivity.class)));
        adminBtn.setOnClickListener(v ->
                startActivity(new Intent(this, AdminActivity.class)));

        // initially disable buttons, show spinner while we figure out the user
        setButtonsEnabled(false);
        showLoading(true);

        //Get firebase auth
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
                        showLoading(false);
                        registerNewUserOverlay();
                    });
        }

    }

    private void showLoading(boolean show) {
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void setButtonsEnabled(boolean enabled) {
        if (attendeeBtn != null) attendeeBtn.setEnabled(enabled);
        if (organizerBtn != null) organizerBtn.setEnabled(enabled);
        if (adminBtn != null) adminBtn.setEnabled(enabled);
    }

    private void checkUserDoc(String uid) {
        fm.getUser(uid, new FirebaseManager.FirebaseCallback<User>() {
            @Override
            public void onSuccess(User user) {
                showLoading(false);
                if (user == null) {
                    if (mainLayout != null) mainLayout.setVisibility(View.GONE);
                    registerNewUserOverlay();
                }
                else {
                    UserEventRepository.getInstance().setUser(user);
                    //if not new user show main_layout
                    if (mainLayout != null) mainLayout.setVisibility(View.VISIBLE);
                    // user loaded  enable buttons
                    setButtonsEnabled(true);
                }
            }
            @Override
            public void onFailure(Exception e) {
                showLoading(false);
                // if something goes wrong, just onboard
                if (mainLayout != null) mainLayout.setVisibility(View.GONE);
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
        Fragment fragment = fm.findFragmentById(containerId);

        if (fragment != null) {
            fm.beginTransaction().remove(fragment).commit();
        }

        View container = findViewById(containerId);
        if (container != null) container.setVisibility(View.GONE);

        // reload user and enable buttons once loaded
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {

            showLoading(true);
            setButtonsEnabled(false);
            checkUserDoc(firebaseUser.getUid());
        }
    }
}