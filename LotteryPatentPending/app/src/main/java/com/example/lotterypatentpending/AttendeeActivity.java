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
import com.example.lotterypatentpending.models.User;
import com.example.lotterypatentpending.viewModels.UserEventRepository;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;





/**
 * Class AttendeeActivity
 * AttendeeActivity hosts the main UI for users attending events.
 * Provides navigation between event browsing, profile management,
 * and QR scanning tabs. Also displays notification inbox count.
 *
 * This activity is shown after a user successfully logs in and
 * has registered their profile.
 *
 * @author Erik
 * @contributor  Erik, Michael
 *
 */
public class AttendeeActivity extends AppCompatActivity {
    /** Repository holding current logged-in user and event state. */
    private UserEventRepository userEventRepo;

    /** Firebase utility for syncing user/event data. */
    private FirebaseManager firebaseManager;

    /** Notification repository for listening to unread message count. */
    private NotificationRepository repo;

    /** Fragment showing events available to the attendee. */
    private Fragment eventsFragment;

    /** Fragment showing the attendee's profile and settings. */
    private Fragment profileFragment;

    /** Fragment handling QR code scanning for check-in. */
    private Fragment scanFragment;




    /**
     * Initializes the attendee dashboard, navigation bar, notification badge,
     * and default fragment. Also sets up Firestore listeners.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.attendee_activity_main);


        userEventRepo = UserEventRepository.getInstance();
        repo = new FirestoreNotificationRepository();

        //Create toolbar and navbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);


        //  make this toolbar the Activity's action bar
        setSupportActionBar(toolbar);

        // home button in header: go back to Main
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
    /**
     * Replaces the fragment container with a new fragment.
     *
     * @param f the fragment to display
     * @return always true for nav handler use
     */
    private boolean load(Fragment f) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.attendeeContainer, f)
                .commit();
        return true;
    }
    /**
     * Sets the title in the top toolbar.
     *
     * @param t title text to display
     */
    private void setTitle(String t) {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(t);
    }
    /**
     * Displays a help popup explaining the lottery selection system
     * for event entry.
     */
    private void showLotteryInfoPopup() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Lottery Information")
                .setMessage(
                        "When registration closes, all entrants on the waiting list are entered into a random draw.\n\n" +
                                "Selected entrants must accept their spot within the time window.\n\n" +
                                "If someone declines or doesn't respond, another entrant is randomly selected.\n\n" +
                                "All eligible entrants have an equal chance, but a spot is not guaranteed."
                )
                .setPositiveButton("OK", null)
                .show();
    }

    /**
     * Inflates toolbar menu and initializes the inbox badge listener
     * to update unread notification count in real time.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_attendee, menu);

        MenuItem inboxItem = menu.findItem(R.id.action_inbox);
        View actionView = inboxItem.getActionView();

        ImageView icon = actionView.findViewById(R.id.inboxIcon);
        View badge  = actionView.findViewById(R.id.badgeDot);

        // open inbox on tap
        actionView.setOnClickListener(v -> {
            // user opened inbox → hide dot immediately
            badge.setVisibility(View.GONE);
            onOptionsItemSelected(inboxItem);
        });

        // get current user (already loaded into UserEventRepository in MainActivity)
        User user = UserEventRepository.getInstance().getUser().getValue();
        if (user == null) {
            badge.setVisibility(View.GONE);
            return true;
        }

        String userId = user.getUserId();

        // Realtime unread badge using NotificationWatcher singleton
        NotificationWatcher.getInstance().startUnreadBadge(
                userId,
                count -> {
                    long c = (count == null) ? 0 : count;
                    runOnUiThread(() -> {
                        boolean hasUnread = c > 0;
                        badge.setVisibility(hasUnread ? View.VISIBLE : View.GONE);
                    });
                },
                err -> android.util.Log.e("Inbox", "listenUnreadCount", err)
        );

        return true;
    }


    @Override
    protected void onPause() {
        super.onPause();
        NotificationWatcher.getInstance().stopUnreadBadge();
    }

    /**
     * Handles toolbar item actions (currently Inbox).
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_inbox) {
            startActivity(new Intent(this, InboxActivity.class));
            return true;
        }

        if(id == R.id.action_info){
            showLotteryInfoPopup();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    /**
     * Removes Firestore listeners to avoid memory leaks when activity stops.
     */
    @Override
    protected void onStop() {
        super.onStop();
        // Stop badge listener when the screen is no longer visible
        NotificationWatcher.getInstance().stopUnreadBadge();
    }

}
