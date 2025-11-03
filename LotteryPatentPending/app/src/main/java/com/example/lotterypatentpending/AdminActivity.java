package com.example.lotterypatentpending;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.lotterypatentpending.models.Notifications;
import com.example.lotterypatentpending.models.RecipientRef;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminActivity extends AppCompatActivity {
    private User currentUser;  // holds the logged-in user
    private FirebaseManager firebaseManager; // Firebase interface

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


    }
    public void removeUserProfile(String userId, User currentUser) {
        if (!currentUser.isAdmin()) {
            Toast.makeText(this, "Access denied: Admin privileges required", Toast.LENGTH_SHORT).show();
            return;
        }

        firebaseManager.deleteUser(userId);
        Toast.makeText(this, "User profile removed", Toast.LENGTH_SHORT).show();
    }
    public void browseAllUsers() {
        FirebaseManager.getInstance().getAllUsers(new FirebaseManager.FirebaseCallback<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot result) {
                List<User> users = new ArrayList<User>();
                for (DocumentSnapshot doc : result) {
                    User user = doc.toObject(User.class);
                    users.add(user);
                }

                // TODO: bind 'users' to a list adapter
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("Admin", "Error loading users: " + e.getMessage());
            }
        });

    }
    public void removeEvent(String eventId, User currentUser) {
        if (!currentUser.isAdmin()) {
            Toast.makeText(this, "Only admins can remove events", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseManager.getInstance().deleteEvent(eventId);
        Toast.makeText(this, "Event removed successfully", Toast.LENGTH_SHORT).show();
    }
    public void browseAllEvents() {
        FirebaseManager.getInstance().getAllEvents(new FirebaseManager.FirebaseCallback<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot result) {
                List<Event> events = new ArrayList<>();
                for (DocumentSnapshot doc : result) {
                    Event event = new Event(
                            doc.getString("title"),
                            doc.getString("description"),
                            ((Long) doc.get("capacity")).intValue()
                    );
                    events.add(event);
                }

                // TODO: display 'events' in  list
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("Admin", "Error loading events: " + e.getMessage());
            }
        });
    }
    private void viewAllNotifications() {
        if (currentUser == null || !currentUser.isAdmin()) {
            Toast.makeText(context, "Access denied: Admin privileges required", Toast.LENGTH_SHORT).show();
            return;
        }

        firebaseManager.getAllNotifications(new FirebaseManager.FirebaseCallback<List<Notifications>>() {
            @Override
            public void onSuccess(List<Notifications> notifications) {
                StringBuilder sb = new StringBuilder("=== Notification Logs ===\n\n");
                for (Notifications n : notifications) {
                    sb.append("Title: ").append(n.getTitle())
                            .append("\nType: ").append(n.getType())
                            .append("\nSender ID: ").append(n.getSenderId())
                            .append("\nRecipients: ");
                    if (n.getRecipients() != null) {
                        for (RecipientRef r : n.getRecipients()) {
                            sb.append(r.getRecipientId()).append(" ");
                        }
                    }
                    sb.append("\nStatus: ").append(n.getStatus())
                            .append("\nCreated At: ").append(n.getCreatedAt())
                            .append("\n\n");
                }

                Log.d("AdminNotifications", sb.toString());
                Toast.makeText(context, "Fetched " + notifications.size() + " notifications", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("Admin", "Error fetching notifications: " + e.getMessage());
            }
        });
    }
}