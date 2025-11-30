package com.example.lotterypatentpending;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lotterypatentpending.models.Event;
import com.example.lotterypatentpending.models.FirebaseManager;
import com.example.lotterypatentpending.models.User;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Allows Admins to view and remove event organizers.
 * This activity loads all events, collects the unique organizer IDs, and then displays
 * the corresponding {@link User} profiles. Admins can delete a user (organizer) via long-press.
 */
public class AdminOrganizersActivity extends AppCompatActivity {

    /** Manages interaction with Firebase Firestore. */
    private FirebaseManager firebaseManager;
    /** ListView displaying the organizers. */
    private ListView listView;
    /** Adapter to link the organizer display names to the ListView. */
    private ArrayAdapter<String> adapter;
    /** Progress bar shown during data loading. */
    private ProgressBar progressBar;

    /** List of {@link User} objects who are organizers. */
    private final List<User> organizerList = new ArrayList<>();
    /** List of formatted strings for display in the ListView. */
    private final List<String> organizerDisplayList = new ArrayList<>();

    /**
     * Initializes the activity, sets up the ListView, and attaches the long-press delete listener.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_organizers);

        listView = findViewById(R.id.userListView);
        progressBar = findViewById(R.id.progressBar); // ✅ spinner
        firebaseManager = FirebaseManager.getInstance();

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, organizerDisplayList);
        listView.setAdapter(adapter);

        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        // Delete on long-press
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            User selectedOrganizer = organizerList.get(position);
            confirmDelete(selectedOrganizer);
            return true;
        });

        loadOrganizersFromFirebase();
    }

    /**
     * Initiates the process to fetch all events to identify unique organizer IDs.
     */
    private void loadOrganizersFromFirebase() {
        progressBar.setVisibility(View.VISIBLE);

        firebaseManager.getAllEvents(new FirebaseManager.FirebaseCallback<ArrayList<Event>>() {
            @Override
            public void onSuccess(ArrayList<Event> events) {
                Set<String> organizerIds = new HashSet<>();

                for (Event e : events) {
                    if (e != null && e.getOrganizer() != null && e.getOrganizer().getUserId() != null) {
                        organizerIds.add(e.getOrganizer().getUserId());
                    }
                }

                fetchUsersAndFilterByOrganizerIds(organizerIds);
            }

            @Override
            public void onFailure(Exception e) {
                progressBar.setVisibility(View.GONE);
                Log.e("AdminOrganizers", "Error loading events", e);
            }
        });
    }

    /**
     * Fetches all user profiles from Firestore and filters them down to the set of known organizers.
     * @param organizerIds The set of user IDs identified as organizers from events.
     */
    private void fetchUsersAndFilterByOrganizerIds(Set<String> organizerIds) {
        firebaseManager.getAllUsers(new FirebaseManager.FirebaseCallback<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot userSnapshot) {
                organizerList.clear();
                organizerDisplayList.clear();

                for (DocumentSnapshot doc : userSnapshot) {
                    if (!organizerIds.contains(doc.getId())) continue;

                    User user = doc.toObject(User.class);
                    if (user == null) continue;

                    user.setUserId(doc.getId());
                    organizerList.add(user);

                    String displayText = user.getName() + " (" + user.getEmail() + ")";
                    if (user.isAdmin()) displayText += " [ADMIN]";
                    organizerDisplayList.add(displayText);
                }

                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Exception e) {
                progressBar.setVisibility(View.GONE);
                Log.e("AdminOrganizers", "Error loading users", e);
            }
        });
    }

    /**
     * Shows a confirmation dialog before deleting an organizer.
     * @param user The {@link User} object to confirm deletion for.
     */
    private void confirmDelete(User user) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Organizer")
                .setMessage("Are you sure you want to delete " + user.getName() + "?\nThis will NOT delete their events.")
                .setPositiveButton("Delete", (dialog, which) -> deleteOrganizer(user))
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Executes the deletion of the organizer's user document in Firestore and refreshes the list.
     * @param user The {@link User} object to delete.
     */
    private void deleteOrganizer(User user) {
        if (user.getUserId() == null) {
            Toast.makeText(this, "Error: User ID missing", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseManager.getInstance().deleteUser(user.getUserId());
        Toast.makeText(this, "Organizer deleted", Toast.LENGTH_SHORT).show();

        // refresh organizer list
        loadOrganizersFromFirebase();
    }
}