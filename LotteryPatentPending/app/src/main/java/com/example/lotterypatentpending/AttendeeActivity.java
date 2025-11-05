package com.example.lotterypatentpending;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.lotterypatentpending.User_interface.Inbox.InboxActivity;
import com.example.lotterypatentpending.models.FirebaseManager;
import com.example.lotterypatentpending.models.NotificationRepository;
import com.example.lotterypatentpending.viewmodels.UserEventRepository;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.ListenerRegistration;

/**
 * Class AttendeeActivity
 * @maintainer Erik
 * @author Erik
 */

public class AttendeeActivity extends AppCompatActivity {
    private UserEventRepository userEventRepo;
    private FirebaseManager  firebaseManager;
    private NotificationRepository repo;
    private ListenerRegistration unreadReg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendee);

        userEventRepo = UserEventRepository.getInstance();
        repo = new com.example.lotterypatentpending.models.NotificationRepository();

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);

        // home button in header: go back to Main
        toolbar.setNavigationIcon(R.drawable.ic_home);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Firebasemanager
        firebaseManager = FirebaseManager.getInstance();

        // default tab = events
        setTitle("Events");
        Fragment default_frag = new AttendeeEventsFragment();
        load(default_frag);

        //creates bottom nav bar and listeners
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_events) {
                setTitle("Events");
                Fragment frag = new AttendeeEventsFragment();
                return load(frag);
            }
            if (id == R.id.nav_profile) {
                setTitle("Profile");
                Fragment frag = new AttendeeProfileFragment();
                return load(frag);
            }
            if (id == R.id.nav_scan) {
                setTitle("Scan");
                Fragment frag = new QRScannerFragment();
                return load(frag);
            }
            return false;
        });
    }

    private boolean load(Fragment f) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.attendeeContainer, f)
                .commit();
        return true;
    }

    private void setTitle(String t) {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(t);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_attendee, menu);
        MenuItem inboxItem = menu.findItem(R.id.action_inbox);
        View actionView = inboxItem.getActionView();

        ImageView icon = actionView.findViewById(R.id.inboxIcon);
        TextView badge = actionView.findViewById(R.id.badgeText);

        // open inbox on tap
        actionView.setOnClickListener(v -> onOptionsItemSelected(inboxItem));

        // listen to unread count
        String userId;
        FirebaseUser current = FirebaseAuth.getInstance().getCurrentUser();
        if (current == null) {
            finish(); // or navigate to login
            return true;
        }
        userId = current.getUid();

        // listen to unread count and keep a reference to remove later
        unreadReg = repo.listenUnreadCount(
                userId,
                count -> {
                    if (count == null || count <= 0) {
                        badge.setVisibility(android.view.View.GONE);
                    } else {
                        badge.setVisibility(android.view.View.VISIBLE);
                        badge.setText(String.valueOf(count));
                    }
                },
                err -> android.util.Log.e("Inbox", "listenUnreadCount", err)
        );

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_inbox) {
            startActivity(new Intent(this, InboxActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
