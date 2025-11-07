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
 * Allows Admins to view and remove event organizers
 */
public class AdminOrganizersActivity extends AppCompatActivity {

    private FirebaseManager firebaseManager;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private ProgressBar progressBar;

    private final List<User> organizerList = new ArrayList<>();
    private final List<String> organizerDisplayList = new ArrayList<>();

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
     * Fetch events → collect organizer IDs → load those users
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
     * Filter Firestore users down to organizer list
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
     * Confirmation dialog
     */
    private void confirmDelete(User user) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Organizer")
                .setMessage("Are you sure you want to delete " + user.getName() + "?\nThis will NOT delete their events.")
                .setPositiveButton("Delete", (dialog, which) -> deleteOrganizer(user))
                .setNegativeButton("Cancel", null)
                .show();
    }

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
