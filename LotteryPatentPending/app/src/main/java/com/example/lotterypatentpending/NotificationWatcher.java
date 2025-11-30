package com.example.lotterypatentpending;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.lotterypatentpending.R;
import com.example.lotterypatentpending.models.FirestoreNotificationRepository;
import com.example.lotterypatentpending.models.Notification;
import com.example.lotterypatentpending.models.NotificationRepository;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Singleton to watch notification-related realtime Firestore streams.
 *
 * - unread badge count for attendee toolbar
 * - popup system notifications when new docs are created
 *
 * @author Moffat
 * @maintainer Moffat
 */
public class NotificationWatcher {

    // ---- functional interfaces for lambdas ----
    @FunctionalInterface
    public interface IntCallback {
        void onValue(@Nullable Integer value);
    }

    @FunctionalInterface
    public interface ErrorCallback {
        void onError(@NonNull Exception e);
    }

    // ---- singleton boilerplate ----
    private static NotificationWatcher instance;

    public static synchronized NotificationWatcher getInstance() {
        if (instance == null) {
            instance = new NotificationWatcher();
        }
        return instance;
    }

    private NotificationWatcher() {}

    // ---- dependencies ----
    private final NotificationRepository repo = new FirestoreNotificationRepository();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Firestore listener for badge
    @Nullable
    private ListenerRegistration unreadBadgeReg;

    // Firestore listener for popup stream
    @Nullable
    private ListenerRegistration popupReg;

    // Track last popup so we don't double-show on config changes
    @Nullable
    private String lastPopupId;

    // Simple counter for system notification IDs
    private final AtomicInteger notifIdCounter = new AtomicInteger(1);

    // ========== BADGE WATCHER ==========

    /**
     * Start watching the unread-count for a user and forward updates
     * to the provided callback. If there's already a watcher active,
     * it is removed first.
     */
    public void startUnreadBadge(@NonNull String userId,
                                 @NonNull IntCallback onCount,
                                 @NonNull ErrorCallback onError) {
        stopUnreadBadge();

        unreadBadgeReg = repo.listenUnreadCount(
                userId,
                count -> onCount.onValue(count),
                e -> onError.onError(e)
        );
    }

    /**
     * Stop watching the unread badge count (e.g. when AttendeeActivity stops).
     */
    public void stopUnreadBadge() {
        if (unreadBadgeReg != null) {
            unreadBadgeReg.remove();
            unreadBadgeReg = null;
        }
    }

    // ========== POPUP STREAM ==========

    /**
     * Starts a realtime listener on users/{uid}/notifications and shows
     * a system notification whenever a NEW unread document is added.
     *
     * Called from MainActivity AFTER the user document is loaded.
     */
    public void startPopupStream(@NonNull Context appContext,
                                 @NonNull String userId) {
        // we want application context for NotificationManager
        final Context ctx = appContext.getApplicationContext();

        // remove previous listener if any
        stopPopupStream();

        popupReg = db.collection("users")
                .document(userId)
                .collection("notifications")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(10)
                .addSnapshotListener((snap, err) -> {
                    if (err != null) {
                        err.printStackTrace();
                        return;
                    }
                    if (snap == null) return;

                    for (DocumentChange dc : snap.getDocumentChanges()) {
                        if (dc.getType() != DocumentChange.Type.ADDED) continue;

                        DocumentSnapshot d = dc.getDocument();
                        String docId = d.getId();

                        // Avoid double popup for same document (e.g. re-attach listener)
                        if (docId.equals(lastPopupId)) continue;
                        lastPopupId = docId;

                        // Only popup for unread notifications
                        Boolean read = d.getBoolean("read");
                        if (Boolean.TRUE.equals(read)) continue;

                        Notification n = d.toObject(Notification.class);
                        if (n == null) continue;

                        showSystemNotification(ctx, n);
                    }
                });
    }

    /**
     * Stop listening for popup notifications.
     * (You can call this from a top-level onDestroy if you ever want to.)
     */
    public void stopPopupStream() {
        if (popupReg != null) {
            popupReg.remove();
            popupReg = null;
        }
    }

    // ========== SYSTEM NOTIFICATION HELPERS ==========

    private static final String CHANNEL_ID = "inbox_high_1";

    private void ensureChannel(Context ctx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(
                    CHANNEL_ID,
                    "Inbox notifications",
                    NotificationManager.IMPORTANCE_HIGH   // 👈 important
            );
            ch.setDescription("Notifications from organizers and lotteries");
            NotificationManager nm = ctx.getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(ch);
        }
    }

    private void showSystemNotification(Context ctx, Notification n) {
        ensureChannel(ctx);

        String title = (n.getTitle() != null && !n.getTitle().isEmpty())
                ? n.getTitle()
                : "New notification";
        String body  = n.getBody() != null ? n.getBody() : "";

        // When the user taps the system notification, open the InboxActivity
        Intent intent = new Intent(ctx, com.example.lotterypatentpending.User_interface.Inbox.InboxActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                ctx,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(ctx, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification_white_24dp)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)   // for < Android 8
                        .setDefaults(NotificationCompat.DEFAULT_ALL)      // 🔔 sound + vibration + lights
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent);
        // <— make it clickable

        NotificationManagerCompat.from(ctx)
                .notify(notifIdCounter.getAndIncrement(), builder.build());
    }

}
