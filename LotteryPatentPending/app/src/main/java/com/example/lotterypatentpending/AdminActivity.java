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
 * AdminActivity provides navigation options for users with admin privileges.
 * From this screen, an administrator can browse users or events in the system.
 * Attempts to access admin-only features without proper permissions are blocked.
 *
 * This activity serves as an entry hub for administrative tasks.
 */
public class AdminActivity extends AppCompatActivity {
    /** Reference to Firebase manager for database operations. */
    private FirebaseManager firebaseManager;
    /** The currently logged-in user. Used to verify admin access. */
    private User currentUser;

    /**
     * Called when the activity is starting. Initializes UI, sets up admin validation,
     * and provides navigation to admin functions such as user browsing and event browsing.
     *
     * @param savedInstanceState previous instance state of the activity, if any.
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
