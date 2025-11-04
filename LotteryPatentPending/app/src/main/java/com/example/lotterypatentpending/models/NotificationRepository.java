package com.example.lotterypatentpending.models;

import androidx.annotation.NonNull;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.*;
import com.example.lotterypatentpending.FirebaseManager;
import com.example.lotterypatentpending.models.Notification;

import java.util.*;
import java.util.function.Consumer;

public class NotificationRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /** Create one Notification doc and fan-out copies to each recipient's inbox */
    public Task<Void> createAndFanOut(@NonNull Notification n) {
        DocumentReference root = db.collection("notifications").document();
        n.setId(root.getId());
        n.setCreatedAt(Timestamp.now());
        if (n.getStatus() == null) n.setStatus("SENT");

        return db.runBatch(batch -> {
            batch.set(root, n);

            for (RecipientRef r : n.getRecipients()) {
                DocumentReference inboxDoc = db.collection("users")
                        .document(r.getUserId())
                        .collection("inbox")
                        .document(n.getId());
                batch.set(inboxDoc, n);
            }
        });
    }

    /** Real-time listener for a user's inbox, newest first. Returns a remover runnable. */
    public ListenerRegistration listenInbox(
            @NonNull String userId,
            @NonNull Consumer<List<Notification>> onChange,
            @NonNull Consumer<Throwable> onError
    ) {
        return db.collection("users").document(userId)
                .collection("inbox")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snap, e) -> {
                    if (e != null) { onError.accept(e); return; }
                    List<Notification> list = new ArrayList<>();
                    if (snap != null) {
                        for (DocumentSnapshot d : snap.getDocuments()) {
                            Notification item = d.toObject(Notification.class);
                            if (item != null) list.add(item);
                        }
                    }
                    onChange.accept(list);
                });
    }

    /** Mark a notification as READ for a user (both in inbox and aggregate root). */
    public Task<Void> markRead(@NonNull String userId, @NonNull String notificationId) {
        DocumentReference inboxDoc = db.collection("users").document(userId)
                .collection("inbox").document(notificationId);
        DocumentReference rootDoc  = db.collection("notifications").document(notificationId);

        return db.runTransaction(tx -> {
            Notification cur = tx.get(inboxDoc).toObject(Notification.class);
            if (cur == null) return null;

            // update status and readBy
            Set<String> readSet = new HashSet<>(cur.getReadBy() == null ? Collections.emptyList() : cur.getReadBy());
            readSet.add(userId);
            cur.setReadBy(new ArrayList<>(readSet));
            cur.setStatus("READ");

            tx.set(inboxDoc, cur);
            tx.set(rootDoc, cur, SetOptions.merge()); // root keeps aggregate
            return null;
        });
    }
    // Create/update one user's notification
    public void addOrUpdateNotification(String userId, Notification n,
                                        FirebaseManager.FirebaseCallback<Void> cb) {
        if (n.getId() == null || n.getId().isEmpty()) {
            n.setId(db.collection("users").document(userId)
                    .collection("inbox").document().getId());
        }
        if (n.getCreatedAt() == null) {
            n.setCreatedAt(com.google.firebase.Timestamp.now());
        }
        if (n.getStatus() == null) {
            n.setStatus("SENT");
        }

        db.collection("users").document(userId)
                .collection("inbox").document(n.getId())
                .set(n)
                .addOnSuccessListener(ignored -> cb.onSuccess(null))
                .addOnFailureListener(cb::onFailure);
    }

    // Realtime unread count (status != READ)
    public com.google.firebase.firestore.ListenerRegistration listenUnreadCount(
            String userId, java.util.function.Consumer<Integer> onCount, java.util.function.Consumer<Throwable> onErr) {

        return db.collection("users").document(userId).collection("inbox")
                .whereNotEqualTo("status", "READ")
                .addSnapshotListener((snap, e) -> {
                    if (e != null) { onErr.accept(e); return; }
                    int count = (snap == null) ? 0 : snap.size();
                    onCount.accept(count);
                });
    }

    // Mark as read per user
    public void markNotificationRead(String userId, String notificationId,
                                     FirebaseManager.FirebaseCallback<Void> cb) {
        com.google.firebase.firestore.DocumentReference ref = db.collection("users")
                .document(userId).collection("inbox").document(notificationId);

        db.runTransaction(tx -> {
                    Notification cur = tx.get(ref).toObject(Notification.class);
                    if (cur == null) return null;
                    cur.setStatus("READ");
                    tx.set(ref, cur);
                    return null;
                }).addOnSuccessListener(ignored -> cb.onSuccess(null))
                .addOnFailureListener(cb::onFailure);
    }
    public com.google.firebase.firestore.ListenerRegistration listenAllNotifications(
            java.util.function.Consumer<java.util.List<com.example.lotterypatentpending.models.Notification>> onChange,
            java.util.function.Consumer<java.lang.Throwable> onError
    ) {
        return db.collection("notifications")
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener((snap, e) -> {
                    if (e != null) { onError.accept(e); return; }
                    java.util.List<com.example.lotterypatentpending.models.Notification> list = new java.util.ArrayList<>();
                    if (snap != null) {
                        for (com.google.firebase.firestore.DocumentSnapshot d : snap.getDocuments()) {
                            com.example.lotterypatentpending.models.Notification n =
                                    d.toObject(com.example.lotterypatentpending.models.Notification.class);
                            if (n != null) list.add(n);
                        }
                    }
                    onChange.accept(list);
                });
    }


}
