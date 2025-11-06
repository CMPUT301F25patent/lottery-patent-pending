package com.example.lotterypatentpending.User_interface.Inbox;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.*;
import com.example.lotterypatentpending.R;
import com.example.lotterypatentpending.models.*;
import com.google.firebase.auth.*;

public class InboxActivity extends AppCompatActivity {
    private final NotificationRepository repo = new FirestoreNotificationRepository();

    @Override protected void onCreate(Bundle b){
        super.onCreate(b); setContentView(R.layout.activity_inbox);
        RecyclerView rv = findViewById(R.id.recycler); rv.setLayoutManager(new LinearLayoutManager(this));
        NotificationAdapter adapter = new NotificationAdapter(n -> {
            if (!n.isRead() && n.getId()!=null) repo.markRead(n.getUserId(), n.getId());
        });
        rv.setAdapter(adapter);

        FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();
        if (u==null){ finish(); return; }
        String uid = u.getUid();

        repo.getForUser(uid).thenAccept(list -> runOnUiThread(() -> {
            for (var n : list) n.setUserId(uid);
            adapter.submitList(list);
        }));

    }

    @Override protected void onStop(){ super.onStop(); }
}

