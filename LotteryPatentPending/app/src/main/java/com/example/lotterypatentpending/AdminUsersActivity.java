/**
 * -----------------------------------------------------------------------------
 * FILE: AdminUsersActivity.java
 * PROJECT: Lottery Patent Pending
 * -----------------------------------------------------------------------------
 * PURPOSE:
 *   Displays a list of registered users to the administrator and allows
 *   for user deletion
 *
 * DESIGN ROLE / PATTERN:
 *   Acts as the View-Controller component in the MVC pattern.
 *   It fetches data through FirebaseManager (Model) and populates the UI.
 *
 * OUTSTANDING ISSUES / LIMITATIONS:
 *   - Deletion currently relies on user ID matching to Firestore document IDs.
 *
 * AUTHOR: Ritvik Das
 * CONTRIBUTORS:
 * -----------------------------------------------------------------------------
 */

package com.example.lotterypatentpending;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lotterypatentpending.models.FirebaseManager;
import com.example.lotterypatentpending.models.User;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The {@code AdminUsersActivity} class provides administrators with an
 * interface to browse and manage registered users. It retrieves user data
 * from Firestore using {@link FirebaseManager} and displays it in a ListView.
 * Administrators can also delete users directly from this screen.
 */

public class AdminUsersActivity extends AppCompatActivity {

    private FirebaseManager firebaseManager;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<User> userList = new ArrayList<>();
    private List<String> userDisplayList = new ArrayList<>();

    /**
     * Initializes the admin user management screen and sets up event listeners
     * for back navigation and user deletion.
     *
     * @param savedInstanceState previously saved state of the activity, if any.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_users);

        listView = findViewById(R.id.userListView);
        firebaseManager = FirebaseManager.getInstance();

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, userDisplayList);
        listView.setAdapter(adapter);

        loadUsersFromFirebase();
        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());



        // Long press to confirm deletion
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            User selectedUser = userList.get(position);

            new AlertDialog.Builder(AdminUsersActivity.this)
                    .setTitle("Delete User")
                    .setMessage("Are you sure you want to delete " + selectedUser.getName() + "?")
                    .setPositiveButton("Delete", (dialog, which) -> deleteUser(selectedUser))
                    .setNegativeButton("Cancel", null)
                    .show();

            return true; // consume long click
        });
    }

    /**
     * Fetches all user data from Firestore via {@link FirebaseManager} and updates the list view.
     */
    private void loadUsersFromFirebase() {
        firebaseManager.getAllUsers(new FirebaseManager.FirebaseCallback<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot result) {
                userList.clear();
                userDisplayList.clear();

                for (DocumentSnapshot doc : result) {
                    User user = doc.toObject(User.class);
                    if (user != null) {
                        user.setUserId(doc.getId());
                        userList.add(user);
                        String displayText = user.getName() + " (" + user.getEmail() + ")";
                        if (user.isAdmin()) displayText += " [ADMIN]";
                        userDisplayList.add(displayText);
                    }
                }

                adapter.notifyDataSetChanged();
                Log.d("BrowseUsers", "Loaded " + userList.size() + " users.");
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("BrowseUsers", "Error loading users: " + e.getMessage());
            }
        });
    }

    private void confirmDelete(User user) {
        new AlertDialog.Builder(this)
                .setTitle("Delete User")
                .setMessage("Are you sure you want to delete " + user.getName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> deleteUser(user))
                .setNegativeButton("Cancel", null)
                .show();
    }
    /**
     * Deletes a user both from Firestore and the on-screen list.
     *
     * @param user the {@link User} to delete.
     */
    private void deleteUser(User user) {
        firebaseManager.deleteUser(user.getUserId());
        Toast.makeText(this, "Deleted user: " + user.getName(), Toast.LENGTH_SHORT).show();
        loadUsersFromFirebase(); // Refresh list after deletion
        adapter.notifyDataSetChanged();
    }
}
