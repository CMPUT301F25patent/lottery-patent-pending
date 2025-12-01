package com.example.lotterypatentpending.User_interface.Inbox;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lotterypatentpending.R;
import com.example.lotterypatentpending.models.FirebaseManager;
import com.example.lotterypatentpending.models.FirestoreNotificationRepository;
import com.example.lotterypatentpending.models.Notification;
import com.example.lotterypatentpending.models.NotificationRepository;
import com.example.lotterypatentpending.models.WaitingListState;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class InboxActivity extends AppCompatActivity {
    private final NotificationRepository repo = new FirestoreNotificationRepository();
    @Nullable
    private ListenerRegistration unreadReg;
    private NotificationAdapter adapter;
    private ListenerRegistration notificationsReg;
    private String currentUserId;

    // Prevent double clicks
    private long lastClickTime = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        RecyclerView rv = findViewById(R.id.recycler);
        rv.setLayoutManager(new LinearLayoutManager(this));

        adapter = new NotificationAdapter(this::onNotificationClicked);
        rv.setAdapter(adapter);

        FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();
        if (u == null) {
            finish();
            return;
        }
        currentUserId = u.getUid();
    }

    private void onNotificationClicked(Notification n) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastClickTime < 500) {
            return;
        }
        lastClickTime = currentTime;

        //Mark as read
        if (!n.isRead() && n.getId() != null) {
            repo.markRead(n.getUserId(), n.getId());
            n.setRead(true);
            int pos = adapter.getCurrentList().indexOf(n);
            if (pos >= 0) adapter.notifyItemChanged(pos);
        }
        boolean isWinCategory = n.getCategory() == Notification.Category.LOTTERY_WIN;
        boolean isWinText = n.getBody() != null && n.getBody().contains("Accept or Decline");

        if (isWinCategory || isWinText) {
            showLotteryWinDialog(n);
            return;
        }

        if (n.getCategory() == Notification.Category.LOTTERY_LOSE) {
            showInfoDialog(n.getTitle(), "You were not selected this time. We will notify you if a spot opens up.");
            return;
        }

        showInfoDialog(n.getTitle(), n.getBody());
    }

    private void showInfoDialog(String title, String body) {
        if (isFinishing()) return;
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(title != null ? title : "Notification")
                .setMessage(body != null ? body : "")
                .setPositiveButton("Close", null)
                .show();
    }

    /**
     * Shows the dialog with ACCEPT and DECLINE buttons.
     */
    private void showLotteryWinDialog(Notification n) {
        if (isFinishing()) return;

        String title = n.getTitle() != null ? n.getTitle() : "Lottery Result";
        String body = (n.getBody() != null ? n.getBody() + "\n\n" : "") +
                "Do you want to accept this spot?";

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(body)
                .setNegativeButton("Decline", (d, w) -> {
                    // Sets state to DECLINED
                    updateWaitingListStates(n, WaitingListState.DECLINED);
                })
                .setPositiveButton("Accept", (d, w) -> {
                    // Sets state to ACCEPTED
                    updateWaitingListStates(n, WaitingListState.ACCEPTED);
                })
                .setCancelable(false)
                .show();
    }

    private void updateWaitingListStates(Notification n, WaitingListState state) {
        String eventId = n.getEventId();
        String userId = n.getUserId();

        if (eventId == null || userId == null) {
            Toast.makeText(this, "Error: Missing event info", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseManager.getInstance().updateWaitingListStates(
                eventId,
                userId,
                state,
                new FirebaseManager.FirebaseCallback<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        runOnUiThread(() -> Toast.makeText(InboxActivity.this, "Response saved.", Toast.LENGTH_SHORT).show());
                    }

                    @Override
                    public void onFailure(Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() -> Toast.makeText(InboxActivity.this, "Failed to update status.", Toast.LENGTH_SHORT).show());
                    }
                }
        );
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (currentUserId == null) return;

        notificationsReg = repo.listenUserNotifications(currentUserId, new NotificationRepository.NotificationsListener() {
            @Override
            public void onChanged(List<Notification> notifications) {
                runOnUiThread(() -> {
                    if (notifications == null || notifications.isEmpty()) {
                        List<Notification> demoList = new ArrayList<>();
                        Notification demo = new Notification();
                        demo.setId("demo");
                        demo.setUserId(currentUserId);
                        demo.setTitle("Welcome!");
                        demo.setBody("Notifications from organizers will appear here.");
                        demo.setCategory(Notification.Category.ORGANIZER_MESSAGE);
                        demo.setCreatedAt(new Date());
                        demoList.add(demo);
                        adapter.submitList(demoList);
                    } else {
                        adapter.submitList(notifications);
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        });

        unreadReg = repo.listenUnreadCount(currentUserId, count -> {}, e -> {});
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (notificationsReg != null) notificationsReg.remove();
        if (unreadReg != null) unreadReg.remove();
    }
}
