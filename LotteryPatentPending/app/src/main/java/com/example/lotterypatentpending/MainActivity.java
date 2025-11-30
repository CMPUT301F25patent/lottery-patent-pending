package com.example.lotterypatentpending;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.content.pm.PackageManager;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;


import com.example.lotterypatentpending.helpers.LoadingOverlay;
import com.example.lotterypatentpending.models.FirebaseManager;
import com.example.lotterypatentpending.models.User;
import com.example.lotterypatentpending.viewModels.UserEventRepository;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Class MainActivity
 * The primary entry point for the application. Handles Firebase initialization,
 * anonymous user sign-in/existing user lookup, user onboarding (if needed),
 * and dynamic navigation based on user roles (Attendee, Organizer, Admin).
 * Also manages the required POST_NOTIFICATIONS permission for Android 13+.
 * @collaborators Erik, Michael
 */
public class MainActivity extends AppCompatActivity implements MainRegisterNewUserFragment.OnProfileSaved {
    /** Manager for Firebase interactions. */
    private FirebaseManager fm;
    /** Button for navigating to the Attendee dashboard. */
    private View attendeeBtn;
    /** Button for navigating to the Organizer dashboard. */
    private View organizerBtn;
    /** Button for navigating to the Admin dashboard. */
    private View adminBtn;
    /** Helper class for managing the full-screen loading spinner. */
    private LoadingOverlay loading;
    /** The main content layout to be hidden during loading/onboarding. */
    private View mainLayout;

    /**
     * Initializes the activity, Firebase, UI components, loading overlay,
     * and performs user authentication/onboarding checks.
     */
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
        mainLayout = findViewById(R.id.main_layout);

        // Inflate loading layout programmatically
        ViewGroup root = findViewById(R.id.main); // FrameLayout root
        View overlayView = getLayoutInflater().inflate(
                R.layout.loading_screen,
                root,
                false    // don't attach immediately
        );
        root.addView(overlayView); // now it sits on top of everything

        loading = new LoadingOverlay(overlayView, mainLayout);
        // start in loading state
        loading.show();

        // click listeners stay the same
        attendeeBtn.setOnClickListener(v ->
                startActivity(new Intent(this, AttendeeActivity.class)));
        organizerBtn.setOnClickListener(v ->
                startActivity(new Intent(this, OrganizerActivity.class)));
        adminBtn.setOnClickListener(v -> handleAdminButtonClick());

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
                        loading.hide();
                        registerNewUserOverlay();
                    });
        }
        // Ask OS for notification permission (Android 13+)
        ensureNotificationPermission();


    }
    /** Request code for POST_NOTIFICATIONS permission. */
    private static final int REQ_POST_NOTIFICATIONS = 1001;

    /**
     * Ensures we have POST_NOTIFICATIONS permission on Android 13+ (Tiramisu).
     */
    private void ensureNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        this,
                        new String[]{ Manifest.permission.POST_NOTIFICATIONS },
                        REQ_POST_NOTIFICATIONS
                );
            }
        }
    }

    /**
     * Checks if the current logged-in user has admin privileges before launching {@link AdminActivity}.
     */
    private void handleAdminButtonClick() {
        User currentUser = UserEventRepository.getInstance().getUser().getValue();

        // Check if user data is loaded and if the user is an admin
        if (currentUser != null && currentUser.isAdmin()) {
            // User is an admin, grant access
            startActivity(new Intent(this, AdminActivity.class));
        } else {
            // User is not an admin or user data isn't loaded yet, deny access
            Toast.makeText(this, "Access Denied: Admin privileges required.", Toast.LENGTH_LONG).show();
        }
    }


    /**
     * Fetches the user document from Firestore. If the document exists, sets the user in the
     * repository and starts the notification stream. If it doesn't exist, initiates the onboarding process.
     * @param uid The Firebase User ID (UID).
     */
    private void checkUserDoc(String uid) {
        loading.show();
        fm.getUser(uid, new FirebaseManager.FirebaseCallback<User>() {
            @Override
            public void onSuccess(User user) {
                loading.hide();
                if (user == null) {
                    if (mainLayout != null) mainLayout.setVisibility(View.GONE);
                    registerNewUserOverlay();
                }
                else {
                    UserEventRepository.getInstance().setUser(user);
                    //if not new user show main_layout
                    if (mainLayout != null) mainLayout.setVisibility(View.VISIBLE);
                    // Start real-time popup listener for this user
                    NotificationWatcher.getInstance().startPopupStream(
                            getApplicationContext(),
                            user.getUserId()
                    );

                }
            }
            @Override
            public void onFailure(Exception e) {
                loading.hide();
                // if something goes wrong, just onboard
                if (mainLayout != null) mainLayout.setVisibility(View.GONE);
                registerNewUserOverlay();
            }
        });
    }

    /**
     * Replaces the content area with the {@link MainRegisterNewUserFragment} to collect
     * the new user's profile details.
     */
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

    /**
     * Callback method called when the user successfully saves their profile in the onboarding fragment.
     * Removes the onboarding fragment and reloads the user data.
     */
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
            loading.show();
            checkUserDoc(firebaseUser.getUid());
        }
    }
}