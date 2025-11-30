package com.example.lotterypatentpending.User_interface.Inbox;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.lotterypatentpending.R;
import com.example.lotterypatentpending.models.FirestoreNotificationRepository;
import com.example.lotterypatentpending.models.Notification;
import com.example.lotterypatentpending.models.NotificationRepository;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * InboxActivity
 * Shows a list of notifications for the currently signed-in user.
 * - Loads notifications via NotificationRepository.getForUser(uid)
 * - Uses NotificationAdapter to render each row
 * - On tap: shows full message in a dialog and marks it as read.
 * @author Moffat
 * @maintainer Moffat
 */

public class InboxActivity extends AppCompatActivity {
    /** The repository used to interact with notification data (e.g., Firestore). */
    private final NotificationRepository repo = new FirestoreNotificationRepository();

    /** Registration object for the real-time listener observing the unread count. */
    @Nullable
    private ListenerRegistration unreadReg;

    /** Adapter for rendering the list of notifications in the RecyclerView. */
    private NotificationAdapter adapter;

    /** Registration object for the real-time listener observing the user's notifications list. */
    private ListenerRegistration notificationsReg;

    /** The unique ID of the currently signed-in user. */
    private String currentUserId;

    /**
     * Initializes the activity, sets up the toolbar and the RecyclerView with its adapter.
     * It checks for the current signed-in user ID.
     * @param savedInstanceState Contains data most recently supplied in {@code onSaveInstanceState(Bundle)}.
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);

        //Toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Back arrow behavior
        toolbar.setNavigationOnClickListener(v -> finish());

        //RecyclerView + adapter
        RecyclerView rv = findViewById(R.id.recycler);
        rv.setLayoutManager(new LinearLayoutManager(this));

        adapter = new NotificationAdapter(n -> {
            // Mark as read
            if (!n.isRead() && n.getId() != null) {
                repo.markRead(n.getUserId(), n.getId());
                n.setRead(true);

                int pos = adapter.getCurrentList().indexOf(n);
                if (pos >= 0) adapter.notifyItemChanged(pos);
            }
        });
        rv.setAdapter(adapter);

        //Load data from Firestore or use demo if empty
        FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();
        if (u == null) {
            // No signed-in user: just close the inbox
            finish();
            return;
        }
        currentUserId = u.getUid();
    }

    /**
     * Called after {@code onCreate(Bundle)} — or after {@code onRestart()} when the activity is
     * re-starting. This is where real-time Firestore listeners are activated.
     */
    @Override
    protected void onStart() {
        super.onStart();
        if (currentUserId == null) {
            finish();
            return;
        }

        // Listens for the user's notifications in real-time
        notificationsReg = repo.listenUserNotifications(
                currentUserId,
                new NotificationRepository.NotificationsListener() {
                    @Override
                    public void onChanged(List<Notification> notifications) {
                        runOnUiThread(() -> {
                            List<Notification> toShow;
                            if (notifications == null || notifications.isEmpty()) {
                                // Demo message if inbox is empty
                                toShow = new ArrayList<>();
                                Notification demo = new Notification();
                                demo.setId("demo");
                                demo.setUserId(currentUserId);
                                demo.setTitle("Welcome to your inbox");
                                demo.setBody("This is a demo notification. Once organizers " +
                                        "send real messages, they’ll appear here.");
                                demo.setCategory(Notification.Category.ORGANIZER_MESSAGE);
                                demo.setCreatedAt(new Date());
                                toShow.add(demo);
                            } else {
                                toShow = notifications;
                            }
                            adapter.submitList(toShow);
                        });
                    }

                    @Override
                    public void onError(Exception e) {
                        e.printStackTrace();
                    }
                }
        );

        // Listens for the real-time unread count (for the badge UI)
        unreadReg = repo.listenUnreadCount(
                currentUserId,
                count -> {
                    android.util.Log.d("Inbox", "Unread count = " + count);
                },
                err -> android.util.Log.e("Inbox", "listenUnreadCount", err)
        );
    }

    /**
     * Called when the activity is no longer visible to the user.
     * This is where all active Firestore listeners are unregistered using {@link ListenerRegistration#remove()}.
     */
    @Override
    protected void onStop() {
        super.onStop();
        if (notificationsReg != null) {
            notificationsReg.remove();
            notificationsReg = null;
        }
        if (unreadReg != null) {
            unreadReg.remove();
            unreadReg = null;
        }
    }
}