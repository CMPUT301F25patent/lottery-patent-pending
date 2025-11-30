package com.example.lotterypatentpending;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;



import com.example.lotterypatentpending.helpers.LoadingOverlay;
import com.example.lotterypatentpending.models.FirebaseManager;
import com.example.lotterypatentpending.models.User;
import com.example.lotterypatentpending.viewModels.UserEventRepository;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
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
    private LoadingOverlay loading;
    private View mainLayout;

    private final int LOCATION_REQ_CODE = 1001;

    @SuppressLint("MissingPermission")
    private final ActivityResultLauncher<String> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    getUserLocation();
                } else {
                    onLocationPermissionDenied();
                }
            });

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
        adminBtn.setVisibility(View.GONE);

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
        adminBtn.setOnClickListener(v -> startActivity(new Intent(this, AdminActivity.class)));

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

    }


    private void checkUserDoc(String uid) {
        loading.show();
        fm.getUser(uid, new FirebaseManager.FirebaseCallback<User>() {
            @Override
            public void onSuccess(User user) {
                loading.hide();
                if (user == null) {
                    if (mainLayout != null) mainLayout.setVisibility(View.GONE);
                    adminBtn.setVisibility(View.GONE);
                    registerNewUserOverlay();
                } else {
                    UserEventRepository.getInstance().setUser(user);
                    if (mainLayout != null) mainLayout.setVisibility(View.VISIBLE);

                    if (user.isAdmin()) {
                        adminBtn.setVisibility(View.VISIBLE);
                    } else {
                        adminBtn.setVisibility(View.GONE);
                    }
                    requestLocationPermission();
                }
            }
            @Override
            public void onFailure(Exception e) {
                loading.hide();
                // if something goes wrong, just onboard
                if (mainLayout != null) mainLayout.setVisibility(View.GONE);
                adminBtn.setVisibility(View.GONE);
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

        // reload user
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            loading.show();
            checkUserDoc(firebaseUser.getUid());
        }
    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED) {

            getUserLocation();

        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void onLocationPermissionDenied() {
        boolean shouldShowRationale =
                shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION);

        if (shouldShowRationale) {
            // User tapped "Deny" (NOT "Never ask again")
            Toast.makeText(this,
                    "Location is required to join this event. Please allow it if you wish to join.",
                    Toast.LENGTH_LONG).show();
        } else {
            // User tapped "Never Ask Again"
            // OR permanently denied in settings
            Toast.makeText(this,
                    "Location permanently denied. Enable it in settings to join this event.",
                    Toast.LENGTH_LONG).show();
        }
    }

    @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    private void getUserLocation() {
        User currentUser  = UserEventRepository.getInstance().getUser().getValue();

        FusedLocationProviderClient client =
                LocationServices.getFusedLocationProviderClient(this);

        client.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        fm.saveLocationToFirestore(currentUser.getUserId(), location.getLatitude(), location.getLongitude());
                    }
                });
    }
}