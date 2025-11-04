package com.example.lotterypatentpending.User_interface.Inbox;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lotterypatentpending.R;
import com.example.lotterypatentpending.models.NotificationRepository;
import com.example.lotterypatentpending.models.Notification;
import com.example.lotterypatentpending.FirebaseManager;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.List;

public class InboxActivity extends AppCompatActivity {

    private NotificationRepository repo;
    private NotificationAdapter adapter;
    private ListenerRegistration reg;
    private String userId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);

        // current user
        FirebaseUser current = FirebaseAuth.getInstance().getCurrentUser();
        if (current == null) { finish(); return; }
        userId = current.getUid();

        repo = new NotificationRepository();

        RecyclerView rv = findViewById(R.id.recycler);
        rv.setLayoutManager(new LinearLayoutManager(this));

        adapter = new NotificationAdapter(n ->
                repo.markNotificationRead(
                        userId,
                        n.getId(),
                        new FirebaseManager.FirebaseCallback<Void>() {
                            @Override public void onSuccess(Void ignored) { /* optional */ }
                            @Override public void onFailure(Exception e) {
                                Log.e("Inbox", "markRead failed", e);
                            }
                        }
                )
        );
        rv.setAdapter(adapter);

        MaterialToolbar tb = findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        tb.setNavigationOnClickListener(v -> finish());

    }

    @Override
    protected void onStart() {
        super.onStart();

        TextView empty = findViewById(R.id.emptyView);
        ProgressBar progress = findViewById(R.id.progress);
        progress.setVisibility(View.VISIBLE);

        reg = repo.listenInbox(
                userId,
                (List<Notification> list) -> {
                    progress.setVisibility(View.GONE);
                    empty.setVisibility((list == null || list.isEmpty()) ? View.VISIBLE : View.GONE);
                    adapter.submit(list);
                },
                (Throwable err) -> {
                    progress.setVisibility(View.GONE);
                    empty.setText("Failed to load inbox");
                    empty.setVisibility(View.VISIBLE);
                    Log.e("Inbox", "listenInbox", err);
                }
        );
    }

    @Override
    protected void onStop() {
        if (reg != null) reg.remove();
        super.onStop();
    }
}


