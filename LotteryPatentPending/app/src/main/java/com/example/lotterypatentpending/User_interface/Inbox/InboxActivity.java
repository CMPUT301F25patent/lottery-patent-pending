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
    private final NotificationRepository repo = new FirestoreNotificationRepository();
    @Nullable
    private ListenerRegistration unreadReg;
    private NotificationAdapter adapter;

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
            // On click: mark as read (if it has an id) and visually fade it
            if (!n.isRead() && n.getId() != null) {
                repo.markRead(n.getUserId(), n.getId());
                n.setRead(true);
                int pos = adapter.getCurrentList().indexOf(n);
                if (pos >= 0) {
                    adapter.notifyItemChanged(pos);
                }
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
        String uid = u.getUid();

        repo.getForUser(uid)
                .thenAccept(list -> runOnUiThread(() -> {
                    List<Notification> toShow;

                    if (list == null || list.isEmpty()) {
                        // DEMO data so the screen isn't blank
                        toShow = new ArrayList<>();
                        Notification sample = new Notification();
                        sample.setId("demo1");
                        sample.setUserId(uid);
                        sample.setTitle("Welcome to your inbox");
                        sample.setBody("This is a demo notification. Once organizers send " +
                                "real messages, they’ll appear here.");
                        sample.setCategory(Notification.Category.ORGANIZER_MESSAGE);
                        sample.setCreatedAt(new Date());
                        toShow.add(sample);
                    } else {
                        // Real data: make sure userId is populated on each notification
                        toShow = list;
                        for (Notification n : toShow) {
                            n.setUserId(uid);
                        }
                    }

                    adapter.submitList(toShow);
                }))
                .exceptionally(e -> {
                    e.printStackTrace();
                    // In case of an error
                    runOnUiThread(() -> {
                        List<Notification> demo = new ArrayList<>();
                        Notification sample = new Notification();
                        sample.setId("error_demo");
                        sample.setUserId(uid);
                        sample.setTitle("Inbox unavailable");
                        sample.setBody("We couldn’t load notifications from the server. " +
                                "This is a demo message so you can still see the layout.");
                        sample.setCategory(Notification.Category.ORGANIZER_MESSAGE);
                        sample.setCreatedAt(new Date());
                        demo.add(sample);
                        adapter.submitList(demo);
                    });
                    return null;
                });

        // unread listener
        unreadReg = repo.listenUnreadCount(uid,
                count -> {  },
                err -> android.util.Log.e("Inbox", "listenUnreadCount", err));
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (unreadReg != null) {
            unreadReg.remove();
            unreadReg = null;
        }
    }
}

