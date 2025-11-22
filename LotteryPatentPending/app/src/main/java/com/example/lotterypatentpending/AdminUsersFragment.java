package com.example.lotterypatentpending;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.lotterypatentpending.helpers.LoadingOverlay;
import com.example.lotterypatentpending.models.FirebaseManager;
import com.example.lotterypatentpending.models.User;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * AdminUsersFragment
 * ------------------
 * Fragment providing administrators with an interface to browse
 * and manage registered users.
 *
 * This is the fragment version of the old AdminUsersActivity:
 *  - Loads all users from Firestore via FirebaseManager
 *  - Displays them in a ListView with "Name (email) [ADMIN]" text
 *  - Long-pressing a user shows a confirm-delete dialog and deletes
 *    the user from Firestore, then refreshes the list.
 */
public class AdminUsersFragment extends Fragment {

    // Firebase service layer (singleton)
    private FirebaseManager firebaseManager;

    // UI references
    private ListView listView;

    // Adapter showing simple strings like "Name (email) [ADMIN]"
    private ArrayAdapter<String> adapter;

    // Raw User objects (full data)
    private final List<User> userList = new ArrayList<>();
    // What we actually display in the ListView
    private final List<String> userDisplayList = new ArrayList<>();

    // Full-screen loading overlay (spinner) reused from other screens
    private LoadingOverlay loading;

    public AdminUsersFragment() {
        // Required empty constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the fragment layout:
        //  - Root: @id/admin_users_root
        //  - ListView: @id/userListView
        return inflater.inflate(R.layout.admin_fragment_users, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        // ----- 1. Find views -----
        listView = v.findViewById(R.id.userListView);
        firebaseManager = FirebaseManager.getInstance();

        // Root layout for this fragment (ConstraintLayout)
        ViewGroup root = v.findViewById(R.id.admin_users_root);

        // ----- 2. Attach the loading overlay -----
        // Reuse the same loading_screen layout as other screens
        View overlayView = getLayoutInflater().inflate(
                R.layout.loading_screen,
                root,
                false
        );
        root.addView(overlayView);

        // Create a LoadingOverlay helper to show/hide this overlay
        loading = new LoadingOverlay(overlayView, null);

        // ----- 3. Set up adapter -----
        // Same idea as AdminUsersActivity: simple text per user
        adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                userDisplayList
        );
        listView.setAdapter(adapter);

        // ----- 4. Load users from Firestore -----
        loading.show();
        loadUsersFromFirebase();

        // ----- 5. Long-press to confirm deletion -----
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            if (position < 0 || position >= userList.size()) return true;

            User selectedUser = userList.get(position);
            confirmDelete(selectedUser);
            return true; // consume long-click
        });
    }

    /**
     * Fetches all user data from Firestore via FirebaseManager and updates the list view.
     */
    private void loadUsersFromFirebase() {
        firebaseManager.getAllUsers(new FirebaseManager.FirebaseCallback<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot result) {
                if (!isAdded()) return;

                userList.clear();
                userDisplayList.clear();

                for (DocumentSnapshot doc : result) {
                    User user = doc.toObject(User.class);
                    if (user != null) {
                        // Make sure the internal userId matches the Firestore doc ID
                        user.setUserId(doc.getId());
                        userList.add(user);

                        String displayText = user.getName() + " (" + user.getEmail() + ")";
                        if (user.isAdmin()) {
                            displayText += " [ADMIN]";
                        }
                        userDisplayList.add(displayText);
                    }
                }

                adapter.notifyDataSetChanged();
                if (loading != null) loading.hide();

                Log.d("AdminUsersFragment", "Loaded " + userList.size() + " users.");
            }

            @Override
            public void onFailure(Exception e) {
                if (!isAdded()) return;

                Log.e("AdminUsersFragment", "Error loading users: " + e.getMessage());
                if (loading != null) loading.hide();

                Toast.makeText(requireContext(),
                        "Failed to load users", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Pops up a confirm-delete dialog for the given user.
     */
    private void confirmDelete(User user) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete User")
                .setMessage("Are you sure you want to delete " + user.getName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> deleteUser(user))
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Deletes a user from Firestore and refreshes the on-screen list.
     *
     * @param user the User to delete.
     */
    private void deleteUser(User user) {
        // Show spinner during delete + reload
        if (loading != null) loading.show();

        firebaseManager.deleteUser(user.getUserId());

        Toast.makeText(requireContext(),
                "Deleted user: " + user.getName(),
                Toast.LENGTH_SHORT).show();

        // Reload from Firestore so userList + userDisplayList stay in sync
        loadUsersFromFirebase();
    }
}
