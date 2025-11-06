package com.example.lotterypatentpending;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

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

public class OrganizerActivity extends AppCompatActivity {
    private OrganizerNotifier organizerNotifier;
    private OrganizerViewModel organizerVm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_organizer_host);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
        var notifRepo = new FirestoreNotificationRepository();
        UserDataSource usersDs = new FirestoreUsersDataSource();
        AdminLogRepository logRepo = new FirestoreAdminLogRepository();
        organizerNotifier = new OrganizerNotifier(notifRepo, usersDs, logRepo);
        organizerVm = new ViewModelProvider(this).get(OrganizerViewModel.class);



    }
    // Call this when the organizer clicks "Send" to a hand-picked set of users.
    private final LotteryResultNotifier resultNotifier = new LotteryResultNotifier();

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

    /** Winners only (if you have a separate button). */
    private void notifyWinnersOnly(String organizerId, String eventId, String eventTitle,
                                   java.util.List<String> winnerIds) {
        resultNotifier.notifyWinners(organizerId, eventId, eventTitle, winnerIds)
                .addOnSuccessListener(v -> Toast.makeText(this, "Notified winners", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> {
                    Log.e("Organizer", "notifyWinnersOnly", e);
                    Toast.makeText(this, "Failed to notify winners", Toast.LENGTH_SHORT).show();
                });
    }

    /** Losers only (if you have a separate button). */
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