package com.example.lotterypatentpending;

/**
 * -----------------------------------------------------------------------------
 * FILE: AdminActivity.java
 * PROJECT: Lottery Patent Pending
 * -----------------------------------------------------------------------------
 * PURPOSE:
 *   Acts as the main control panel for administrators. This activity provides
 *   access to administrative functions such as browsing and managing users
 *   and events. It serves as a gateway to other admin-specific screens.
 *
 * DESIGN ROLE / PATTERN:
 *   - Serves as a Controller in the MVC pattern, mediating between user input
 *     (UI buttons) and navigation to management views (fragments/activities).
 *   - Utilizes the Singleton instance of FirebaseManager for database operations,
 *     though this activity primarily handles navigation and access control.
 *
 * OUTSTANDING ISSUES / LIMITATIONS:
 *   - Currently uses a hardcoded admin user for testing; session-based user
 *     authentication should replace this in production.
 *   - No verification mechanism yet to ensure admin privileges persist across
 *     activity transitions.
 *   - “Home” behaviour is simulated via back navigation (toolbar + bottom nav).
 *
 * AUTHOR: Ritvik Das
 * COLLABORATORS: Erik Bacsa
 * -----------------------------------------------------------------------------
 */

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.lotterypatentpending.models.FirebaseManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AdminActivity extends AppCompatActivity {

    private FirebaseManager firebaseManager;

    // Root menu vs fragment container
    private LinearLayout adminContent;
    private FrameLayout adminFragmentContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_activity_main);

        firebaseManager = FirebaseManager.getInstance();

        adminContent = findViewById(R.id.adminContent);
        adminFragmentContainer = findViewById(R.id.adminFragmentContainer);

        // Start on main admin menu
        adminContent.setVisibility(View.VISIBLE);
        adminFragmentContainer.setVisibility(View.GONE);

        // --- Toolbar setup ---
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Admin Panel");
        }

        // Toolbar nav go to main on home click
        toolbar.setNavigationOnClickListener(v -> finish());

        // Bottom nav as a Back button
        BottomNavigationView bottomNav = findViewById(R.id.adminBottomNav);
        if (bottomNav != null) {
            bottomNav.setOnItemSelectedListener(item -> {
                getOnBackPressedDispatcher().onBackPressed();
                return true;
            });
        }

        FragmentManager fm = getSupportFragmentManager();

        // Toggle menu vs fragment whenever back stack changes
        fm.addOnBackStackChangedListener(() -> {
            if (fm.getBackStackEntryCount() == 0) {
                // No fragments => show menu, hide fragment container
                adminContent.setVisibility(View.VISIBLE);
                adminFragmentContainer.setVisibility(View.GONE);
            } else {
                // Fragment(s) on stack => show fragment container, hide menu
                adminContent.setVisibility(View.GONE);
                adminFragmentContainer.setVisibility(View.VISIBLE);
            }
        });

        // Handle system back / gestures with dispatcher
        getOnBackPressedDispatcher().addCallback(this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        FragmentManager fm = getSupportFragmentManager();
                        if (fm.getBackStackEntryCount() > 0) {
                            // If we’re in a fragment, go back to the admin menu
                            fm.popBackStack();
                        } else {
                            // On main admin screen -> do NOTHING
                            // (no finish(), stay in AdminActivity)
                        }
                    }
                });

        // Buttons
        Button btnBrowseUsers       = findViewById(R.id.btnBrowseUsers);
        Button btnBrowseEvents      = findViewById(R.id.btnBrowseEvents);
        Button btnLog               = findViewById(R.id.btnLog);
        Button btnImages            = findViewById(R.id.btnImages);
        Button btnRemoveOrganizers  = findViewById(R.id.btnRemoveOrganizers);

        // Users: show AdminUsersFragment
        if (btnBrowseUsers != null) {
            btnBrowseUsers.setOnClickListener(v ->
                    showFragment(new AdminUsersFragment(), "AdminUsersFragment"));
        }

        // Events: show AdminEventsFragment
        if (btnBrowseEvents != null) {
            btnBrowseEvents.setOnClickListener(v ->
                    showFragment(new AdminEventsFragment(), "AdminEventsFragment"));
        }

        // Notification log: still its own Activity
        if (btnLog != null) {
            btnLog.setOnClickListener(v -> {
                Intent intent = new Intent(AdminActivity.this, NotificationAdminActivity.class);
                startActivity(intent);
            });
        }

        // Stubbed
        if (btnImages != null) {
            btnImages.setOnClickListener(v -> {
                // TODO: open admin images screen
            });
        }

        // Stubbed
        if (btnRemoveOrganizers != null) {
            btnRemoveOrganizers.setOnClickListener(v -> {
                // TODO: open remove-organizer screen
            });
        }
    }

    /**
     * Replaces the fragment container with the provided fragment and
     * pushes the transaction onto the back stack.
     */
    private void showFragment(Fragment fragment, String tag) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.adminFragmentContainer, fragment, tag)
                .addToBackStack(tag)
                .commit();
    }
}