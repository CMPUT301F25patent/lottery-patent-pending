package com.example.lotterypatentpending;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.lotterypatentpending.data.FirestoreUsersDataSource;
import com.example.lotterypatentpending.data.UserDataSource;
import com.example.lotterypatentpending.domain.OrganizerNotifier;
import com.example.lotterypatentpending.models.AdminLogRepository;
import com.example.lotterypatentpending.models.FirestoreAdminLogRepository;
import com.example.lotterypatentpending.models.FirestoreNotificationRepository;
import com.example.lotterypatentpending.models.LotteryResultNotifier;
import com.example.lotterypatentpending.viewModels.OrganizerViewModel;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.concurrent.CompletableFuture;

/**
 * Activity used by event organizers to manage notifications for lottery results,
 * waitlisted users, selected participants, and cancelled events.
 * <p>
 * Handles UI initialization, edge-to-edge setup, and provides helper methods
 * for sending notifications to specific groups of users via {@link OrganizerNotifier}
 * and {@link LotteryResultNotifier}.
 * </p>
 */
public class OrganizerActivity extends AppCompatActivity {
    /** Helper class for sending general notifications to groups of users. */
    private OrganizerNotifier organizerNotifier;
    /** ViewModel to hold and manage UI-related data for organizer fragments. */
    private OrganizerViewModel organizerVm;
    // Call this when the organizer clicks "Send" to a hand-picked set of users.
    /** Helper class for handling notifications specifically related to lottery results (winners/losers). */
    private final LotteryResultNotifier resultNotifier = new LotteryResultNotifier();

    /**
     * Initializes the activity, sets up the toolbar and navigation components,
     * and initializes the notifiers and ViewModel.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_activity_host);

        //Shared toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Make the home icon do something (e.g., finish organizer & go back)
        toolbar.setNavigationOnClickListener(v -> finish());

        // Shared bottom nav
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavOrganizer);

        // NavController for all organizer fragments
        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.organizer_nav_host);
        NavController navController = navHostFragment.getNavController();

        // Handle bottom nav actions (e.g., Back)
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_back) {
                navController.popBackStack();
                return true;
            }
            // add other nav items here if you want
            return false;
        });

        // (Optional) update toolbar title from destination labels
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (destination.getId() == R.id.CreateEditEventFragment) {
                boolean isEdit = arguments != null && arguments.getBoolean("isEdit", false);

                toolbar.setTitle(isEdit ? "Edit Event" : "Create Event");
                return;
            }

            if (destination.getLabel() != null) {
                toolbar.setTitle(destination.getLabel());
            }
        });

        var notifRepo = new FirestoreNotificationRepository();
        UserDataSource usersDs = new FirestoreUsersDataSource();
        AdminLogRepository logRepo = new FirestoreAdminLogRepository();
        organizerNotifier = new OrganizerNotifier(notifRepo, usersDs, logRepo);
        organizerVm = new ViewModelProvider(this).get(OrganizerViewModel.class);

    }

    /**
     * Sends a message to a hand-picked list of users.
     *
     * @param organizerId      UID of the organizer sending the notification.
     * @param eventId          ID of the event.
     * @param selectedUserIds  List of user IDs to notify.
     * @param eventTitle       Title of the event (used in message content).
     */
    private void sendMessageToSelectedUsers(String organizerId,
                                            String eventId,
                                            java.util.List<String> selectedUserIds,
                                            String eventTitle) {
        resultNotifier.notifyWinners(organizerId, eventId, eventTitle, selectedUserIds)
                .addOnSuccessListener(v ->
                        android.widget.Toast.makeText(this, "Notification sent", android.widget.Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> {
                    android.util.Log.e("Organizer","notifyWinners", e);
                    android.widget.Toast.makeText(this, "Failed to send", android.widget.Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Notifies all waitlisted users for a given event.
     *
     * @param organizerId UID of the organizer sending notifications.
     * @param eventId     Event ID.
     * @param title       Title of the notification.
     * @param body        Body content of the notification.
     */
    private void notifyAllWaitlist(String organizerId, String eventId, String title, String body) {
        organizerNotifier.notifyAllWaitlist(organizerId, eventId, title, body)
                .thenAccept(ids -> runOnUiThread(() ->
                        android.widget.Toast.makeText(this, "Notified " + ids.size() + " waitlisted", android.widget.Toast.LENGTH_SHORT).show()))
                .exceptionally(e -> { runOnUiThread(() ->
                        android.widget.Toast.makeText(this, "Failed to notify waitlist", android.widget.Toast.LENGTH_SHORT).show());
                    android.util.Log.e("Organizer", "notifyAllWaitlist", e);
                    return null;
                });
    }

    /**
     * Notifies all selected users for a given event.
     *
     * @param organizerId UID of the organizer sending notifications.
     * @param eventId     Event ID.
     * @param title       Title of the notification.
     * @param body        Body content of the notification.
     */
    private void notifyAllSelected(String organizerId, String eventId, String title, String body) {
        organizerNotifier.notifyAllSelected(organizerId, eventId, title, body)
                .thenAccept(ids -> runOnUiThread(() ->
                        android.widget.Toast.makeText(this, "Notified " + ids.size() + " selected", android.widget.Toast.LENGTH_SHORT).show()))
                .exceptionally(e -> { runOnUiThread(() ->
                        android.widget.Toast.makeText(this, "Failed to notify selected", android.widget.Toast.LENGTH_SHORT).show());
                    android.util.Log.e("Organizer", "notifyAllSelected", e);
                    return null;
                });
    }

    /**
     * Notifies all cancelled participants for a given event.
     *
     * @param organizerId UID of the organizer sending notifications.
     * @param eventId     Event ID.
     * @param title       Title of the notification.
     * @param body        Body content of the notification.
     */
    private void notifyAllCancelled(String organizerId, String eventId, String title, String body) {
        organizerNotifier.notifyAllCancelled(organizerId, eventId, title, body)
                .thenAccept(ids -> runOnUiThread(() ->
                        android.widget.Toast.makeText(this, "Notified " + ids.size() + " cancelled", android.widget.Toast.LENGTH_SHORT).show()))
                .exceptionally(e -> { runOnUiThread(() ->
                        android.widget.Toast.makeText(this, "Failed to notify cancelled", android.widget.Toast.LENGTH_SHORT).show());
                    android.util.Log.e("Organizer", "notifyAllCancelled", e);
                    return null;
                });
    }
    /**
     * Call this right after you've saved winners to Firestore
     * (e.g., in your existing "Publish/Finalize" button handler).
     *
     * It notifies winners AND losers and shows one "done" toast
     * when both tasks complete. If you only want winners (or only losers),
     * see the helpers below.
     *
     * Publishes lottery results with notifications for both winners and losers.
     * <p>
     * Sends notifications for winners, calculates losers from the pool, and notifies them.
     * Shows a single Toast message when both tasks complete.
     * </p>
     *
     * @param organizerId current organizer's uid
     * @param eventId     event document id
     * @param eventTitle  used for message text ("You're selected for ...")
     * @param allEntrantIds full pool considered in the draw (for losers)
     * @param winnerIds      the selected winners
     */
    private void publishResultsWithNotifications(
            String organizerId,
            String eventId,
            String eventTitle,
            java.util.List<String> allEntrantIds,
            java.util.List<String> winnerIds
    ) {
        // 1) notify winners (WIN). Returns a Firebase Task<Void>.
        Task<Void> tWinners = resultNotifier
                .notifyWinners(organizerId, eventId, eventTitle, winnerIds)
                .addOnFailureListener(e -> Log.e("Organizer", "notifyWinners failed", e));

        // 2) notify losers (LOSE) = (allEntrantIds - winnerIds), filtered by opt-in.
        Task<Void> tLosers = resultNotifier
                .notifyLosersFromPool(organizerId, eventId, eventTitle, allEntrantIds, winnerIds)
                .addOnFailureListener(e -> Log.e("Organizer", "notifyLosersFromPool failed", e));

        // 3) wait for both to finish before telling the user we're done
        Tasks.whenAllComplete(tWinners, tLosers)
                .addOnSuccessListener(done ->
                        Toast.makeText(this, "Results sent (winners + losers)", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e -> {
                    // This only fires if whenAll itself errors (rare). Individual errors are logged above.
                    Log.e("Organizer", "publishResultsWithNotifications whenAll failed", e);
                    Toast.makeText(this, "Some notifications failed", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Notifies only the winners of the lottery results.
     * @param organizerId current organizer's uid
     * @param eventId event document id
     * @param eventTitle used for message text
     * @param winnerIds the selected winners
     */
    private void notifyWinnersOnly(String organizerId, String eventId, String eventTitle,
                                   java.util.List<String> winnerIds) {
        resultNotifier.notifyWinners(organizerId, eventId, eventTitle, winnerIds)
                .addOnSuccessListener(v -> Toast.makeText(this, "Notified winners", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> {
                    Log.e("Organizer", "notifyWinnersOnly", e);
                    Toast.makeText(this, "Failed to notify winners", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Notifies only the losers (non-winners) of the lottery results.
     * @param organizerId current organizer's uid
     * @param eventId event document id
     * @param eventTitle used for message text
     * @param allEntrantIds full pool considered in the draw
     * @param winnerIds the selected winners (used to calculate losers)
     */
    private void notifyLosersOnly(String organizerId, String eventId, String eventTitle,
                                  java.util.List<String> allEntrantIds, java.util.List<String> winnerIds) {
        resultNotifier.notifyLosersFromPool(organizerId, eventId, eventTitle, allEntrantIds, winnerIds)
                .addOnSuccessListener(v -> Toast.makeText(this, "Notified losers", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> {
                    Log.e("Organizer", "notifyLosersOnly", e);
                    Toast.makeText(this, "Failed to notify losers", Toast.LENGTH_SHORT).show();
                });
    }


}