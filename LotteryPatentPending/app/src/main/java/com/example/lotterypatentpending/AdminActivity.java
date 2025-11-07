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
 *     (UI buttons) and navigation to management activities.
 *   - Utilizes the Singleton instance of FirebaseManager for database operations,
 *     though this activity primarily handles navigation and access control.
 *
 * OUTSTANDING ISSUES / LIMITATIONS:
 *   - Currently uses a hardcoded admin user for testing; session-based user
 *     authentication should replace this in production.
 *   - No verification mechanism yet to ensure admin privileges persist across
 *     activity transitions.
 *   - Home button only finishes the activity instead of returning to a true
 *     “home” screen.
 *
 * AUTHOR: Ritvik Das
 * COLLABORATORS:
 * -----------------------------------------------------------------------------
 */

package com.example.lotterypatentpending;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.lotterypatentpending.models.FirebaseManager;
import com.example.lotterypatentpending.models.User;

/**
 * The {@code AdminActivity} class represents the main administrator dashboard.
 * It allows an admin user to navigate to user management and event management
 * screens. Access to these functions is restricted to users with admin privileges.
 */

public class AdminActivity extends AppCompatActivity {

    private FirebaseManager firebaseManager;
    private User currentUser;
    /**
     * Initializes the admin dashboard screen.
     * Sets up UI elements, Firebase access, and button listeners for navigation.
     *
     * @param savedInstanceState Saved state from a previous instance (if any).
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin);

        firebaseManager = FirebaseManager.getInstance();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // For testing — in reality this will be passed in from login/session
        currentUser = new User("admin001", "System Admin", "admin@email.com", "N/A", true);

        Button btnBrowseUsers = findViewById(R.id.btnBrowseUsers);
        Button btnBrowseEvents = findViewById(R.id.btnBrowseEvents);
        ImageButton homeButton = findViewById(R.id.homeButton);
        homeButton.setOnClickListener(v -> finish());


        btnBrowseUsers.setOnClickListener(v -> {
            if (!currentUser.isAdmin()) {
                Toast.makeText(this, "Access denied: Admin privileges required", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(AdminActivity.this, AdminUsersActivity.class);
            startActivity(intent);
        });

        btnBrowseEvents.setOnClickListener(v -> {
            if (!currentUser.isAdmin()) {
                Toast.makeText(this, "Access denied: Admin privileges required", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(AdminActivity.this, AdminEventsActivity.class);
            startActivity(intent);
        });
    }
}
