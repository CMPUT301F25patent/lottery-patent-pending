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
import com.example.lotterypatentpending.models.FirebaseManager;
import com.example.lotterypatentpending.models.WaitingListState;
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
    private final NotificationRepository repo = new FirestoreNotificationRepository();
    @Nullable
    private ListenerRegistration unreadReg;
    private NotificationAdapter adapter;
    private ListenerRegistration notificationsReg;
    private String currentUserId;

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
            // Common: mark as read if needed
            if (!n.isRead() && n.getId() != null) {
                repo.markRead(n.getUserId(), n.getId());
                n.setRead(true);
                int pos = adapter.getCurrentList().indexOf(n);
                if (pos >= 0) adapter.notifyItemChanged(pos);
            }

            // Branch on category
            switch (n.getCategory()) {
                case LOTTERY_WIN:
                    showLotteryWinDialog(n);
                    break;
                case LOTTERY_LOSE:
                    showInfoDialog(
                            n.getTitle(),
                            n.getBody() != null ? n.getBody()
                                    : "You weren’t selected this time. We’ll keep you posted if anything changes."
                    );
                    break;
                default:
                    showInfoDialog(
                            n.getTitle(),
                            n.getBody()
                    );
                    break;
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

    private void showInfoDialog(String title, String body) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(title != null ? title : "Notification")
                .setMessage(body != null ? body : "")
                .setPositiveButton("OK", null)
                .show();
    }

    private void showLotteryWinDialog(Notification n) {
        String title = n.getTitle() != null ? n.getTitle() : "Lottery result";
        String body = (n.getBody() != null ? n.getBody() + "\n\n" : "") +
                "Do you want to accept this spot?";

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(body)
                .setNegativeButton("Can’t attend", (d, w) -> {
                    updateWaitingListStates(n, WaitingListState.CANCELED);
                })
                .setPositiveButton("Accept spot", (d, w) -> {
                    updateWaitingListStates(n, WaitingListState.ACCEPTED);
                })
                .show();
    }

    private void updateWaitingListStates(Notification n, WaitingListState state) {
        String eventId = n.getEventId();   // make sure Notification has this field
        String userId  = n.getUserId();

        if (eventId == null || userId == null) {
            // Nothing we can do – but don’t crash
            return;
        }

        FirebaseManager.getInstance().updateWaitingListStates(
                eventId,
                userId,
                state,
                new FirebaseManager.FirebaseCallback<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        runOnUiThread(() ->
                                android.widget.Toast.makeText(
                                        InboxActivity.this,
                                        "Your response has been saved.",
                                        android.widget.Toast.LENGTH_SHORT
                                ).show()
                        );
                    }

                    @Override
                    public void onFailure(Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() ->
                                android.widget.Toast.makeText(
                                        InboxActivity.this,
                                        "Failed to update your status. Please try again.",
                                        android.widget.Toast.LENGTH_SHORT
                                ).show()
                        );
                    }
                }
        );
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (currentUserId == null) {
            finish();
            return;
        }

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

        // REAL-TIME unread count (for the badge – the badge UI)
        unreadReg = repo.listenUnreadCount(
                currentUserId,
                count -> {
                    android.util.Log.d("Inbox", "Unread count = " + count);
                },
                err -> android.util.Log.e("Inbox", "listenUnreadCount", err)
        );
    }

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

