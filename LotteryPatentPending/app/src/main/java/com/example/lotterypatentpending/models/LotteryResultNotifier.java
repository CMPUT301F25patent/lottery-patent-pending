package com.example.lotterypatentpending.models;

import androidx.annotation.NonNull;

import com.example.lotterypatentpending.data.FirestoreUsersDataSource;
import com.example.lotterypatentpending.data.UserDataSource;
import com.example.lotterypatentpending.domain.EntrantResultNotifier;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Coordinates "winner"/"loser" result notifications for a lottery run.
 *
 * <p>Public API returns {@link Task} so Activities can use
 * addOnSuccessListener/addOnFailureListener. Internally it uses the pure-Java
 * domain service {@link EntrantResultNotifier} and a {@link UserDataSource}
 * to filter recipients who have opted-in to notifications.</p>
 *
 * <p>Writes:
 * <ul>
 *   <li>users/{uid}/notifications/{id} (WIN/LOSE)</li>
 *   <li>admin/notificationsLog/records/{logId} (audit trail)</li>
 * </ul>
 * </p>
 */
public class LotteryResultNotifier {

    private final UserDataSource usersDs;
    private final EntrantResultNotifier entrantSvc;

    /**
     * Convenience constructor using Firestore implementations.
     */
    public LotteryResultNotifier() {
        NotificationRepository notifRepo = new FirestoreNotificationRepository();
        AdminLogRepository logRepo = new FirestoreAdminLogRepository();
        this.usersDs = new FirestoreUsersDataSource();
        this.entrantSvc = new com.example.lotterypatentpending.domain.EntrantResultNotifier(notifRepo, logRepo);
    }

    /**
     * DI-friendly constructor (useful in tests).
     */
    public LotteryResultNotifier(@NonNull UserDataSource usersDs,
                                 @NonNull EntrantResultNotifier entrantSvc) {
        this.usersDs = Objects.requireNonNull(usersDs);
        this.entrantSvc = Objects.requireNonNull(entrantSvc);
    }

    /**
     * Notify all winners (US 01.04.01).
     *
     * <p>Filters {@code winnerIds} by user opt-in, then sends each a WIN
     * notification with a human-friendly title/body. Completes successfully when
     * all writes finish; fails if any write fails.</p>
     *
     * @param organizerId organizer UID (author of the message)
     * @param eventId     the event the lottery belongs to
     * @param eventTitle  event title to include in the message
     * @param winnerIds   list of selected entrant UIDs
     * @return Firebase {@link Task} that completes when all WIN notifications are written
     */
    public Task<Void> notifyWinners(@NonNull String organizerId,
                                    @NonNull String eventId,
                                    @NonNull String eventTitle,
                                    @NonNull List<String> winnerIds) {
        // Message content (adjust as your team prefers)
        String title = "You're selected for " + eventTitle + "!";
        String body  = "Congrats! You've been selected. Check the event page for next steps.";

        CompletableFuture<Void> cf = usersDs.filterOptedIn(eventId, winnerIds)
                .thenCompose(optedIn -> fanOutWin(organizerId, eventId, title, body, optedIn));
        return toTask(cf);
    }

    /**
     * Notify all non-winners from a provided entrant pool (US 01.04.02).
     *
     * <p>Computes losers = allEntrantIds - winnerIds, filters by opt-in, then sends
     * a LOSE notification to each.</p>
     *
     * @param organizerId  organizer UID
     * @param eventId      event ID
     * @param eventTitle   event title for message context
     * @param allEntrantIds all entrants considered in the draw
     * @param winnerIds     the winners selected from the pool
     * @return Task that completes when all LOSE notifications are written
     */
    public Task<Void> notifyLosersFromPool(@NonNull String organizerId,
                                           @NonNull String eventId,
                                           @NonNull String eventTitle,
                                           @NonNull List<String> allEntrantIds,
                                           @NonNull List<String> winnerIds) {
        String title = "Lottery result for " + eventTitle;
        String body  = "You weren’t selected this time. We’ll notify you if a spot opens.";

        // losers = all - winners
        Set<String> all = new HashSet<>(allEntrantIds);
        all.removeAll(new HashSet<>(winnerIds));
        List<String> losers = new ArrayList<>(all);

        CompletableFuture<Void> cf = usersDs.filterOptedIn(eventId, losers)
                .thenCompose(optedIn -> fanOutLose(organizerId, eventId, title, body, optedIn));
        return toTask(cf);
    }

    //internals

    private CompletableFuture<Void> fanOutWin(String org, String evt, String title, String body, List<String> recipients) {
        // send to zero users is still "success"
        if (recipients.isEmpty()) return CompletableFuture.completedFuture(null);

        List<CompletableFuture<Void>> writes = new ArrayList<>(recipients.size());
        for (String uid : recipients) {
            writes.add(entrantSvc.notifyWin(org, evt, uid, title, body));
        }
        return CompletableFuture.allOf(writes.toArray(new CompletableFuture[0]));
    }

    private CompletableFuture<Void> fanOutLose(String org, String evt, String title, String body, List<String> recipients) {
        if (recipients.isEmpty()) return CompletableFuture.completedFuture(null);

        List<CompletableFuture<Void>> writes = new ArrayList<>(recipients.size());
        for (String uid : recipients) {
            writes.add(entrantSvc.notifyLose(org, evt, uid, title, body));
        }
        return CompletableFuture.allOf(writes.toArray(new CompletableFuture[0]));
    }

    /** Bridge: convert a {@link CompletableFuture} into a Firebase {@link Task}. */
    private static <T> Task<T> toTask(CompletableFuture<T> cf) {
        TaskCompletionSource<T> tcs = new TaskCompletionSource<>();
        cf.whenComplete((val, err) -> {
            if (err != null) {
                // unwrap CompletionException if present
                Throwable cause = (err instanceof java.util.concurrent.CompletionException && err.getCause()!=null)
                        ? err.getCause() : err;
                tcs.setException((Exception) cause);
            } else {
                tcs.setResult(val);
            }
        });
        return tcs.getTask();
    }
}


