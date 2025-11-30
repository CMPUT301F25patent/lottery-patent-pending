package com.example.lotterypatentpending.domain;

import androidx.annotation.NonNull;

import com.example.lotterypatentpending.data.UserDataSource;
import com.example.lotterypatentpending.models.*;
import com.example.lotterypatentpending.models.Notification.Category;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Application service for organizer-triggered fan-outs (broadcasts).
 *
 * <p>Coordinates:
 * <ul>
 *   <li>look up entrant ids via {@link UserDataSource}</li>
 *   <li>filter by notifications opt-in</li>
 *   <li>fan-out {@link Notification}s via {@link NotificationRepository}</li>
 *   <li>write an {@link NotificationLog} for admin auditing</li>
 * </ul>
 *
 * @author Moffat
 * @maintainer Moffat
 */

public class OrganizerNotifier {

    private final NotificationRepository notifRepo;
    private final UserDataSource        usersDs;
    private final AdminLogRepository    logRepo;
    private final FirebaseFirestore     db = FirebaseFirestore.getInstance();

    public OrganizerNotifier(@NonNull NotificationRepository notifRepo,
                             @NonNull UserDataSource usersDs,
                             @NonNull AdminLogRepository logRepo) {
        this.notifRepo = notifRepo;
        this.usersDs   = usersDs;
        this.logRepo   = logRepo;
    }


    /**
     * Reads events/{eventId}.waitingList and returns the UIDs of entrants whose
     * state is in {@code allowedStates}.
     */
    private CompletableFuture<List<String>> loadEntrantsByState(
            @NonNull String eventId,
            @NonNull Set<WaitingListState> allowedStates
    ) {
        CompletableFuture<List<String>> f = new CompletableFuture<>();

        db.collection("events").document(eventId)
                .get()
                .addOnSuccessListener((DocumentSnapshot snap) -> {
                    List<String> out = new ArrayList<>();

                    // OrganizerNotifier.java – inside loadEntrantsByState(...)
                    Object raw = snap.get("waitingList");
                    if (raw instanceof java.util.Map) {
                        @SuppressWarnings("unchecked")
                        java.util.Map<String, Object> map = (java.util.Map<String, Object>) raw;

                        for (java.util.Map.Entry<String, Object> e : map.entrySet()) {
                            String uid = String.valueOf(e.getKey());
                            Object v   = e.getValue();

                            if (v instanceof java.util.Map) {
                                @SuppressWarnings("unchecked")
                                java.util.Map<String, Object> row = (java.util.Map<String, Object>) v;
                                Object st = row.get("state");

                                if (st != null) {
                                    try {
                                        WaitingListState state =
                                                WaitingListState.valueOf(String.valueOf(st));
                                        if (allowedStates.contains(state)) {
                                            out.add(uid);
                                        }
                                    } catch (IllegalArgumentException ignore) {
                                        // unknown enum string — ignore
                                    }
                                }
                            }
                        }
                    }


                    f.complete(out);
                })
                .addOnFailureListener(f::completeExceptionally);

        return f;
    }

    /**
     * Fan-out helper: write one notification per recipient + admin log.
     */
    private CompletableFuture<List<String>> sendToRecipients(
            String organizerId,
            String eventId,
            String title,
            String body,
            List<String> recipientIds,
            Notification.Category category
    ) {
        if (recipientIds.isEmpty()) {
            return CompletableFuture.completedFuture(recipientIds);
        }

        List<CompletableFuture<String>> writes = new ArrayList<>();

        for (String uid : recipientIds) {
            Notification n = new Notification();
            n.setUserId(uid);
            n.setSenderId(organizerId);
            n.setEventId(eventId);
            n.setTitle(title);
            n.setBody(body);
            n.setCategory(category);

            writes.add(notifRepo.createForUser(uid, n));
        }

        // admin log entry
        NotificationLog log = new NotificationLog();
        log.setOrganizerId(organizerId);
        log.setEventId(eventId);
        log.setRecipientIds(recipientIds);
        log.setCategory(category);
        log.setPayloadPreview(body);

        writes.add(logRepo.record(log).thenApply(v -> "log"));


        return CompletableFuture
                .allOf(writes.toArray(new CompletableFuture[0]))
                .thenApply(unused -> recipientIds);
    }

    /**
     * "Chosen entrants to sign up"
     *
     * We interpret this as: everyone currently in state SELECTED
     * (they've been drawn but not yet ACCEPTED / DECLINED / CANCELED).
     */
    public CompletableFuture<List<String>> notifyChosenToSignup(
            String organizerId,
            String eventId,
            String title,
            String body
    ) {
        return loadEntrantsByState(
                eventId,
                EnumSet.of(WaitingListState.SELECTED)
        ).thenCompose(ids ->
                sendToRecipients(
                        organizerId,
                        eventId,
                        title,
                        body,
                        ids,
                        Notification.Category.ORGANIZER_MESSAGE
                )
        );
    }

    /**
     * "All waitlisted entrants"
     *
     * Everyone who is still in the pool but not currently holding a spot.
     * -> ENTERED + NOT_SELECTED
     */
    public CompletableFuture<List<String>> notifyAllWaitlist(
            String organizerId,
            String eventId,
            String title,
            String body
    ) {
        return loadEntrantsByState(
                eventId,
                EnumSet.of(WaitingListState.ENTERED, WaitingListState.NOT_SELECTED)
        ).thenCompose(ids ->
                sendToRecipients(
                        organizerId,
                        eventId,
                        title,
                        body,
                        ids,
                        Notification.Category.WAITLIST
                )
        );
    }

    /**
     * "All selected entrants"
     *
     * Everyone who currently holds a spot: SELECTED or ACCEPTED.
     * (If your team prefers only ACCEPTED, swap the EnumSet.)
     */
    public CompletableFuture<List<String>> notifyAllSelected(
            String organizerId,
            String eventId,
            String title,
            String body
    ) {
        return loadEntrantsByState(
                eventId,
                EnumSet.of(WaitingListState.SELECTED, WaitingListState.ACCEPTED)
        ).thenCompose(ids ->
                sendToRecipients(
                        organizerId,
                        eventId,
                        title,
                        body,
                        ids,
                        Notification.Category.SELECTED
                )
        );
    }

    /**
     * "All cancelled entrants"
     * Everyone who is out of the running: CANCELED or DECLINED.
     */
    public CompletableFuture<List<String>> notifyAllCancelled(
            String organizerId,
            String eventId,
            String title,
            String body
    ) {
        return loadEntrantsByState(
                eventId,
                EnumSet.of(WaitingListState.CANCELED, WaitingListState.DECLINED)
        ).thenCompose(ids ->
                sendToRecipients(
                        organizerId,
                        eventId,
                        title,
                        body,
                        ids,
                        Notification.Category.CANCELLED
                )
        );
    }
}


