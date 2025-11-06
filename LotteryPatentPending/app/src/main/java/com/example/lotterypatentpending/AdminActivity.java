package com.example.lotterypatentpending;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.lotterypatentpending.User_interface.Inbox.NotificationAdapter;
import com.example.lotterypatentpending.models.Event;
import com.example.lotterypatentpending.models.FirebaseManager;
import com.example.lotterypatentpending.models.FirestoreAdminLogRepository;
import com.example.lotterypatentpending.models.Notification;
import com.example.lotterypatentpending.models.NotificationLog;
import com.example.lotterypatentpending.models.NotificationRepository;
import com.example.lotterypatentpending.models.RecipientRef;
import com.example.lotterypatentpending.models.User;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class AdminActivity extends AppCompatActivity {
    private User currentUser;  // holds the logged-in user
    // --- UI ---
    private SwipeRefreshLayout swipe;
    private RecyclerView recycler;
    private View emptyState;
    private View progress;

    // --- Data ---
    private final FirestoreAdminLogRepository repo = new FirestoreAdminLogRepository();
    private final AdminNotifAdapter adapter = new AdminNotifAdapter();
    private FirebaseManager firebaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_notif);

        // Toolbar w/ back arrow (the XML should set app:navigationIcon or you can set it here)
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Views
        swipe = findViewById(R.id.swipe);
        recycler = findViewById(R.id.recycler);
        emptyState = findViewById(R.id.emptyState);
        progress = findViewById(R.id.progress);

        // Recycler setup
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recycler.setAdapter(adapter);
        recycler.setHasFixedSize(true);

        // Pull-to-refresh
        swipe.setOnRefreshListener(this::loadData);

        // Initial load
        loadData();

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
    /**
     * Fetch logs from Firestore and bind to the list.
     * Shows/hides progress + empty states appropriately.
     */
    private void loadData() {
        showLoading(true);
        repo.getAllLogs()
                .thenAccept(logs -> runOnUiThread(() -> {
                    List<NotificationLog> safe = (logs == null) ? new ArrayList<>() : logs;
                    adapter.submit(safe);
                    showLoading(false);
                    showEmpty(safe.isEmpty());
                    swipe.setRefreshing(false);
                }))
                .exceptionally(err -> {
                    runOnUiThread(() -> {
                        showLoading(false);
                        showEmpty(true);
                        swipe.setRefreshing(false);
                        Toast.makeText(this, "Failed to load logs", Toast.LENGTH_SHORT).show();
                    });
                    return null;
                });
    }

    private void showLoading(boolean show) {
        progress.setVisibility(show ? View.VISIBLE : View.GONE);
        // keep list visible during swipe refresh; progress is for first load or manual refresh
    }

    private void showEmpty(boolean show) {
        emptyState.setVisibility(show ? View.VISIBLE : View.GONE);
        recycler.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    // ---------------- Adapter ----------------
    private static class AdminNotifAdapter extends RecyclerView.Adapter<AdminNotifAdapter.VH> {
        private final List<NotificationLog> data = new ArrayList<>();
        private final DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);

        void submit(@NonNull List<NotificationLog> items) {
            data.clear();
            data.addAll(items);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View row = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_admin_notif, parent, false);
            return new VH(row);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            NotificationLog n = data.get(position);

            // Title: Category • EventId
            String cat = (n.getCategory() == null) ? "" : n.getCategory().name();
            String evt = (n.getEventId() == null) ? "" : n.getEventId();
            String title = cat.isEmpty() ? evt : (evt.isEmpty() ? cat : (cat + " • " + evt));
            h.title.setText(title);

            // Body: payload preview
            h.body.setText(n.getPayloadPreview() == null ? "" : n.getPayloadPreview());

            // Meta: organizerId • when
            String who = (n.getOrganizerId() == null) ? "" : n.getOrganizerId();
            String when = (n.getCreatedAt() == null) ? "" : df.format(n.getCreatedAt());
            h.meta.setText(TextUtils.isEmpty(who) ? when : (who + " • " + when));
        }

        @Override
        public int getItemCount() { return data.size(); }

        static class VH extends RecyclerView.ViewHolder {
            final TextView title, body, meta;
            VH(@NonNull View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.title);
                body  = itemView.findViewById(R.id.body);
                meta  = itemView.findViewById(R.id.meta);
            }
        }
    }

}