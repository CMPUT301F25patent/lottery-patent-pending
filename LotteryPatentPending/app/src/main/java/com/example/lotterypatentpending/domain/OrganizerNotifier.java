package com.example.lotterypatentpending.domain;

import android.util.Log;
import androidx.annotation.NonNull;
import com.example.lotterypatentpending.data.UserDataSource;
import com.example.lotterypatentpending.models.AdminLogRepository;
import com.example.lotterypatentpending.models.FirestoreAdminLogRepository;
import com.example.lotterypatentpending.models.Notification;
import com.example.lotterypatentpending.models.NotificationLog;
import com.example.lotterypatentpending.models.NotificationRepository;
import com.example.lotterypatentpending.models.WaitingListState;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class OrganizerNotifier {
    private final NotificationRepository notifRepo;
    private final UserDataSource usersDs;
    private final AdminLogRepository logRepo;
    private static final String TAG = "OrganizerNotifier";

    public OrganizerNotifier(@NonNull NotificationRepository notifRepo,
                             @NonNull UserDataSource usersDs) {
        this.notifRepo = Objects.requireNonNull(notifRepo);
        this.usersDs = Objects.requireNonNull(usersDs);
        this.logRepo = new FirestoreAdminLogRepository();
    }

    public CompletableFuture<List<String>> notifySelectedToSignUp(String orgId, String eventId, String eventTitle, String body) {
        return usersDs.getEntrantsByState(eventId, WaitingListState.SELECTED)
                .thenCompose(userIds -> fanOut(orgId, eventId, "Update for " + eventTitle, body, userIds, Notification.Category.SELECTED));
    }

    public CompletableFuture<List<String>> notifyEntrantsInWaitlist(String orgId, String eventId, String eventTitle, String body) {
        return usersDs.getEntrantsByState(eventId, WaitingListState.ENTERED)
                .thenCompose(userIds -> fanOut(orgId, eventId, "Update for " + eventTitle, body, userIds, Notification.Category.WAITLIST));
    }

    public CompletableFuture<List<String>> notifyEntrantsAttending(String orgId, String eventId, String eventTitle, String body) {
        return usersDs.getEntrantsByState(eventId, WaitingListState.ACCEPTED)
                .thenCompose(userIds -> fanOut(orgId, eventId, "Update for " + eventTitle, body, userIds, Notification.Category.ORGANIZER_MESSAGE));
    }

    public CompletableFuture<List<String>> notifyCancelledEntrants(String orgId, String eventId, String eventTitle, String body) {
        return usersDs.getEntrantsByState(eventId, WaitingListState.CANCELED)
                .thenCompose(userIds -> fanOut(orgId, eventId, "Update for " + eventTitle, body, userIds, Notification.Category.CANCELLED));
    }

    private CompletableFuture<List<String>> fanOut(String org, String evt, String title, String body, List<String> userIds, Notification.Category cat) {
        if (userIds.isEmpty()) {
            Log.w(TAG, "fanOut: No users found for category " + cat);
            return CompletableFuture.completedFuture(new ArrayList<>());
        }

        List<String> notifiedIds = new ArrayList<>();
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (String uid : userIds) {
            if (uid == null) continue;
            Notification n = new Notification(uid, evt, org, title, body, cat);
            futures.add(notifRepo.add(n).thenRun(() -> {
                synchronized (notifiedIds) { notifiedIds.add(uid); }
            }));
        }

        NotificationLog log = new NotificationLog(org, evt, cat, userIds, body);
        futures.add(logRepo.record(log));

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenApply(v -> notifiedIds);
    }
}