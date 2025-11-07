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
import com.example.lotterypatentpending.models.FirestoreNotificationRepository;
import com.example.lotterypatentpending.models.NotificationRepository;
import com.example.lotterypatentpending.viewModels.UserEventRepository;
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

    private Fragment eventsFragment;
    private Fragment profileFragment;
    private Fragment scanFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendee);


        userEventRepo = UserEventRepository.getInstance();
        repo = new FirestoreNotificationRepository();

        //Create toolbar and navbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);

        // home button in header: go back to Main
        toolbar.setNavigationIcon(R.drawable.ic_home);
        toolbar.setNavigationOnClickListener(v -> finish());

        //Firebasemanager get db instance
        firebaseManager = FirebaseManager.getInstance();


        eventsFragment = new AttendeeEventsFragment();
        profileFragment = new AttendeeProfileFragment();
        scanFragment = new AttendeeQRScannerFragment();

        // default tab
        setTitle("Events");
        load(eventsFragment);

        //creates bottom nav bar and listeners
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_events) {
                setTitle("Events");
                return load(eventsFragment);
            }
            if (id == R.id.nav_profile) {
                setTitle("Profile");
                return load(profileFragment);
            }
            if (id == R.id.nav_scan) {
                setTitle("Scan");
                return load(scanFragment);
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


    // Toolbar menu with inbox badge
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_attendee, menu);

        // custom action layout with badge
        MenuItem inboxItem = menu.findItem(R.id.action_inbox);
        View actionView = inboxItem.getActionView();

        ImageView icon = actionView.findViewById(R.id.inboxIcon);
        TextView badge  = actionView.findViewById(R.id.badgeText);

        // open inbox on tap
        actionView.setOnClickListener(v -> onOptionsItemSelected(inboxItem));

        // listen to unread count
        FirebaseUser current = FirebaseAuth.getInstance().getCurrentUser();
        if (current == null) { finish(); return true; }
        String userId = current.getUid();

        // keep a reference so we can remove() in onStop()
        unreadReg = repo.listenUnreadCount(
                userId,
                count -> {
                    int c = (count == null) ? 0 : count;
                    badge.setVisibility(c <= 0 ? View.GONE : View.VISIBLE);
                    if (c > 0) badge.setText(String.valueOf(c));
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

    @Override
    protected void onStop() {
        super.onStop();
        if (unreadReg != null) { unreadReg.remove(); unreadReg = null; }
    }
}
