package com.example.lotterypatentpending.models;

import android.util.Log;
import androidx.annotation.NonNull;
import com.example.lotterypatentpending.data.FirestoreUsersDataSource;
import com.example.lotterypatentpending.data.UserDataSource;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Coordinates "winner"/"loser" result notifications.
 * Manually enforces LOTTERY_WIN categories.
 * Records Admin Logs for audit purposes.
 */
public class LotteryResultNotifier {

    private final UserDataSource usersDs;
    private final NotificationRepository notifRepo;
    private final AdminLogRepository logRepo;

    public LotteryResultNotifier() {
        this.notifRepo = new FirestoreNotificationRepository();
        this.usersDs = new FirestoreUsersDataSource();
        this.logRepo = new FirestoreAdminLogRepository();
    }

    public LotteryResultNotifier(@NonNull UserDataSource usersDs,
                                 @NonNull NotificationRepository notifRepo,
                                 @NonNull AdminLogRepository logRepo) {
        this.usersDs = Objects.requireNonNull(usersDs);
        this.notifRepo = Objects.requireNonNull(notifRepo);
        this.logRepo = Objects.requireNonNull(logRepo);
    }

    public Task<Void> notifyWinners(@NonNull String organizerId,
                                    @NonNull String eventId,
                                    @NonNull String eventTitle,
                                    @NonNull List<String> winnerIds) {
        String title = "Update for " + eventTitle;
        String body  = "You have been selected to attend! Please choose to Accept or Decline this invitation.";

        Log.d("LotteryNotifier", "Preparing WIN notifications for " + winnerIds.size() + " users.");

        return toTask(fanOutWin(organizerId, eventId, title, body, winnerIds));
    }

    public Task<Void> notifyLosersFromPool(@NonNull String organizerId,
                                           @NonNull String eventId,
                                           @NonNull String eventTitle,
                                           @NonNull List<String> allEntrantIds,
                                           @NonNull List<String> winnerIds) {
        String title = "Update for " + eventTitle;
        String body  = "You were not selected in the lottery this time. We will notify you if a spot opens up.";

        Set<String> all = new HashSet<>(allEntrantIds);
        all.removeAll(new HashSet<>(winnerIds));
        List<String> losers = new ArrayList<>(all);

        Log.d("LotteryNotifier", "Preparing LOSE notifications for " + losers.size() + " users.");

        return toTask(fanOutLose(organizerId, eventId, title, body, losers));
    }

    private CompletableFuture<Void> fanOutWin(String org, String evt, String title, String body, List<String> recipients) {
        if (recipients.isEmpty()) return CompletableFuture.completedFuture(null);

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        // 1. Send individual notifications
        for (String uid : recipients) {
            Notification n = new Notification(uid, evt, org, title, body, Notification.Category.LOTTERY_WIN);
            futures.add(notifRepo.add(n));
        }

        // 2. Write ONE Admin Log entry for the batch
        NotificationLog log = new NotificationLog(org, evt, Notification.Category.LOTTERY_WIN, recipients, body);
        futures.add(logRepo.record(log));

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    private CompletableFuture<Void> fanOutLose(String org, String evt, String title, String body, List<String> recipients) {
        if (recipients.isEmpty()) return CompletableFuture.completedFuture(null);

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        // 1. Send individual notifications
        for (String uid : recipients) {
            Notification n = new Notification(uid, evt, org, title, body, Notification.Category.LOTTERY_LOSE);
            futures.add(notifRepo.add(n));
        }

        // 2. Write ONE Admin Log entry for the batch
        NotificationLog log = new NotificationLog(org, evt, Notification.Category.LOTTERY_LOSE, recipients, body);
        futures.add(logRepo.record(log));

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    private static <T> Task<T> toTask(CompletableFuture<T> cf) {
        TaskCompletionSource<T> tcs = new TaskCompletionSource<>();
        cf.whenComplete((val, err) -> {
            if (err != null) {
                Throwable cause = (err instanceof java.util.concurrent.CompletionException && err.getCause()!=null) ? err.getCause() : err;
                tcs.setException((Exception) cause);
            } else {
                tcs.setResult(val);
            }
        });
        return tcs.getTask();
    }
}
