package com.example.lotterypatentpending;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lotterypatentpending.User_interface.Inbox.NotificationAdapter;
import com.example.lotterypatentpending.models.Event;
import com.example.lotterypatentpending.models.Notification;
import com.example.lotterypatentpending.models.NotificationRepository;
import com.example.lotterypatentpending.models.RecipientRef;
import com.example.lotterypatentpending.models.User;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AdminActivity extends AppCompatActivity {
    private User currentUser;  // holds the logged-in user
    private com.example.lotterypatentpending.models.FirebaseManager firebaseManager; // Firebase interface
    private NotificationRepository repo;
    private NotificationAdapter adapter;
    private ListenerRegistration reg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_notif);

        firebaseManager = com.example.lotterypatentpending.models.FirebaseManager.getInstance();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        repo = new NotificationRepository();
        RecyclerView rv = findViewById(R.id.recycler);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationAdapter(n -> {
            // Admin tap could open a details screen; no markRead here.
        });
        rv.setAdapter(adapter);

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
        com.example.lotterypatentpending.models.FirebaseManager.getInstance().getAllUsers(new com.example.lotterypatentpending.models.FirebaseManager.FirebaseCallback<QuerySnapshot>() {
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

        com.example.lotterypatentpending.models.FirebaseManager.getInstance().deleteEvent(eventId);
        Toast.makeText(this, "Event removed successfully", Toast.LENGTH_SHORT).show();
    }
    public void browseAllEvents() {
        com.example.lotterypatentpending.models.FirebaseManager.getInstance().getAllEvents(new com.example.lotterypatentpending.models.FirebaseManager.FirebaseCallback<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot result) {
                List<Event> events = new ArrayList<>();

                for (DocumentSnapshot doc : result) {
                    try {
                        // Title, description, and capacity
                        String title = doc.getString("title");
                        String description = doc.getString("description");

                        Long capLong = doc.getLong("capacity");
                        int capacity = (capLong != null) ? capLong.intValue() : 0;

                        // Organizer — stored as a subdocument or simple map
                        User organizer = null;
                        Map<String, Object> organizerMap = (Map<String, Object>) doc.get("organizer");
                        if (organizerMap != null) {
                            String organizerId = (String) organizerMap.get("userId");
                            String organizerName = (String) organizerMap.get("name");
                            String organizerEmail = (String) organizerMap.get("email");
                            String contactInfo = (String) organizerMap.get("contactInfo");

                            organizer = new User(organizerId, organizerName, organizerEmail, contactInfo, false);
                        }

                        Event event = new Event(title, description, capacity, organizer);

                        // Optional
                        String dateStr = doc.getString("date");
                        String timeStr = doc.getString("time");
                        String location = doc.getString("location");
                        event.setLocation(location);


                        events.add(event);

                    } catch (Exception e) {
                        Log.e("Admin", "Error parsing event: " + e.getMessage());
                    }
                }

                Log.d("Admin", "Loaded " + events.size() + " events");

                // TODO: display 'events' in a list or
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("Admin", "Error loading events: " + e.getMessage());
            }
        });
    }
    private void viewAllNotifications() {
        if (currentUser == null || !currentUser.isAdmin()) {
            Toast.makeText(this, "Access denied: Admin privileges required", Toast.LENGTH_SHORT).show();
            return;
        }

        firebaseManager.getAllNotifications(new com.example.lotterypatentpending.models.FirebaseManager.FirebaseCallback<List<Notification>>() {
            @Override
            public void onSuccess(List<Notification> notifications) {
                StringBuilder sb = new StringBuilder("=== Notification Logs ===\n\n");
                for (Notification n : notifications) {
                    sb.append("Title: ").append(n.getTitle())
                            .append("\nType: ").append(n.getType())
                            .append("\nSender ID: ").append(n.getSenderId())
                            .append("\nRecipients: ");
                    if (n.getRecipients() != null) {
                        for (RecipientRef r : n.getRecipients()) {
                            sb.append(r.getUserId()).append(" ");
                        }
                    }
                    sb.append("\nStatus: ").append(n.getStatus())
                            .append("\nCreated At: ").append(n.getCreatedAt())
                            .append("\n\n");
                }

                Log.d("AdminNotifications", sb.toString());
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("Admin", "Error fetching notifications: " + e.getMessage());
            }
        });
    }
    public void removeOrganizerByEvent(String eventId, User currentUser) {
        if (currentUser == null || !currentUser.isAdmin()) {
            Toast.makeText(this, "Access denied: Admin privileges required", Toast.LENGTH_SHORT).show();
            return;
        }

        com.example.lotterypatentpending.models.FirebaseManager.getInstance().getEventById(eventId, new com.example.lotterypatentpending.models.FirebaseManager.FirebaseCallback<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot doc) {
                if (!doc.exists()) {
                    Toast.makeText(AdminActivity.this, "Event not found", Toast.LENGTH_SHORT).show();
                    return;
                }
                Event event = doc.toObject(Event.class);
                if (event == null || event.getOrganizer() == null) {
                    Toast.makeText(AdminActivity.this, "Event has no linked organizer", Toast.LENGTH_SHORT).show();
                    return;
                }

                User organizer = event.getOrganizer();
                com.example.lotterypatentpending.models.FirebaseManager.getInstance().deleteUser(organizer.getUserId());
                Toast.makeText(AdminActivity.this, "Organizer removed successfully", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("Admin", "Error fetching event: " + e.getMessage());
                Toast.makeText(AdminActivity.this, "Failed to retrieve event", Toast.LENGTH_SHORT).show();
            }
        });
    }
}