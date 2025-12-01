package com.example.lotterypatentpending;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.lotterypatentpending.R;
import com.example.lotterypatentpending.User_interface.Inbox.InboxActivity;
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

    private static final String CHANNEL_ID = "inbox_channel";
    private static NotificationWatcher INSTANCE;

    private final NotificationRepository repo = new FirestoreNotificationRepository();
    private ListenerRegistration unreadReg;
    private ListenerRegistration popupReg;
    private Context appContext;

    private NotificationWatcher() {}

    public static synchronized NotificationWatcher getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new NotificationWatcher();
        }
        return INSTANCE;
    }

    //UNREAD BADGE

    public interface CountCallback {
        void onCount(Long count);
    }
    public interface ErrorCallback {
        void onError(Exception e);
    }

    /**
     * Start listening for unread count updates for the badge dot.
     */
    public void startUnreadBadge(@NonNull String userId,
                                 @NonNull CountCallback onCount,
                                 @NonNull ErrorCallback onError) {
        stopUnreadBadge();
        unreadReg = repo.listenUnreadCount(
                userId,
                count -> {
                    Long c = (count == null) ? 0L : count;
                    onCount.onCount(c);
                },
                onError::onError
        );
    }

    public void stopUnreadBadge() {
        if (unreadReg != null) {
            unreadReg.remove();
            unreadReg = null;
        }
    }

    //POPUP STREAM

    /**
     * Listens in real time to the newest notifications for this user and
     * shows a banner when a NEW unread notification is added.
     */
    public void startPopupStream(@NonNull Context context,
                                 @NonNull String userId) {
        appContext = context.getApplicationContext();
        ensureChannel(appContext);

        // Clear any old listener
        if (popupReg != null) {
            popupReg.remove();
            popupReg = null;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        popupReg = db.collection("users")
                .document(userId)
                .collection("notifications")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(10)   // safety
                .addSnapshotListener((snap, err) -> {
                    if (err != null || snap == null) return;

                    for (DocumentChange change : snap.getDocumentChanges()) {
                        if (change.getType() != DocumentChange.Type.ADDED) continue;

                        Notification n = change.getDocument().toObject(Notification.class);
                        if (n == null) continue;
                        if (n.isRead()) continue;  // don't pop for already-read

                        showSystemNotification(n);
                    }
                });
    }

    public void stopPopupStream() {
        if (popupReg != null) {
            popupReg.remove();
            popupReg = null;
        }
    }

    private void ensureChannel(Context ctx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm =
                    (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm.getNotificationChannel(CHANNEL_ID) == null) {
                NotificationChannel ch = new NotificationChannel(
                        CHANNEL_ID,
                        "Inbox messages",
                        NotificationManager.IMPORTANCE_HIGH
                );
                nm.createNotificationChannel(ch);
            }
        }
    }

    private void showSystemNotification(@NonNull Notification n) {
        if (appContext == null) return;

        // Runtime permission check for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    appContext,
                    Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        // Tap banner → open Inbox
        Intent intent = new Intent(appContext, InboxActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(
                appContext,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(appContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_white_24dp) // use your bell icon here
                .setContentTitle(n.getTitle())
                .setContentText(n.getBody())
                .setStyle(new NotificationCompat.BigTextStyle().bigText(n.getBody()))
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setContentIntent(pi);

        // Use a random-ish id so multiple notifications can stack
        int id = (int) (System.currentTimeMillis() & 0xFFFFFFF);
        NotificationManagerCompat.from(appContext).notify(id, builder.build());
    }
}
