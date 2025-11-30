package com.example.lotterypatentpending.models;

import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Firestore-backed {@link NotificationRepository}.
 *
 * <p>Collection layout:
 * users/{userId}/notifications/{notificationId}
 *
 * @author Moffat
 * @maintainer Moffat
 */

public class FirestoreNotificationRepository implements NotificationRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    /**
     * Adds a new notification to a user's notification sub-collection.
     * @param n The notification object to add.
     * @return A CompletableFuture that completes when the operation is finished.
     */
    @Override public CompletableFuture<Void> add(Notification n){
        var f=new CompletableFuture<Void>();
        db.collection("users").document(n.getUserId()).collection("notifications")
                .add(n).addOnSuccessListener(r->f.complete(null)).addOnFailureListener(f::completeExceptionally);
        return f;
    }
    /**
     * Marks a specific notification as read.
     * This method fetches all notifications for a user and updates the one matching the ID.
     * @param userId The ID of the user.
     * @param id The ID of the notification document to mark as read.
     * @return A CompletableFuture that completes when the update is finished.
     */
    //if you pass the document id, update directly—it’s cheaper than scanning.
    @Override public CompletableFuture<Void> markRead(String userId,String id){
        var f=new CompletableFuture<Void>();
        db.collection("users").document(userId).collection("notifications").get()
                .addOnSuccessListener(snap -> {
                    // find by id field if not using documentId
                    for (var d: snap.getDocuments()) {
                        if (id != null && id.equals(d.getId())) {
                            d.getReference().update("read", true).addOnSuccessListener(v -> f.complete(null))
                                    .addOnFailureListener(f::completeExceptionally);
                            return;
                        }
                    }
                    f.complete(null);
                }).addOnFailureListener(f::completeExceptionally);
        return f;
    }

    /**
     * Retrieves all notifications for a specific user, ordered with the newest first.
     * @param userId The ID of the user whose notifications are to be fetched.
     * @return A CompletableFuture that completes with a list of notifications.
     */
    @Override
    public CompletableFuture<List<Notification>> getForUser(String userId){
        var f = new CompletableFuture<List<Notification>>();
        db.collection("users").document(userId).collection("notifications")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snap -> {
                    java.util.ArrayList<Notification> list = new java.util.ArrayList<>();
                    for (var d : snap.getDocuments()) {
                        Notification n = d.toObject(Notification.class);
                        if (n != null) {
                            n.setId(d.getId());       //ensure doc id present
                            n.setUserId(userId);
                            list.add(n);
                        }
                    }
                    f.complete(list);
                })
                .addOnFailureListener(f::completeExceptionally);
        return f;
    }

    /**
     * Listens for real-time changes to the count of unread notifications for a user.
     * @param userId The ID of the user.
     * @param onCount A consumer to be called with the updated unread count.
     * @param onError A consumer to handle any errors.
     * @return A ListenerRegistration to detach the listener when no longer needed.
     */
    @Override public ListenerRegistration listenUnreadCount(String userId,
                                                            java.util.function.Consumer<Integer> onCount,
                                                            java.util.function.Consumer<Exception> onError){
        return db.collection("users").document(userId).collection("notifications")
                .whereEqualTo("read", false)
                .addSnapshotListener((snap, err) -> {
                    if (err!=null){ onError.accept(err); return; }
                    onCount.accept(snap==null?0:snap.size());
                });
    }

    @Override
    public ListenerRegistration listenUserNotifications(String userId,
                                                        NotificationsListener listener) {
        return db.collection("users")
                .document(userId)
                .collection("notifications")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snap, err) -> {
                    if (err != null) {
                        listener.onError(err);
                        return;
                    }
                    List<Notification> out = new ArrayList<>();
                    if (snap != null) {
                        for (DocumentSnapshot d : snap.getDocuments()) {
                            Notification n = d.toObject(Notification.class);
                            if (n != null) {
                                n.setId(d.getId());
                                n.setUserId(userId);
                                out.add(n);
                            }
                        }
                    }
                    listener.onChanged(out);
                });
    }

    @Override
    public CompletableFuture<String> createForUser(String userId, Notification n) {
        CompletableFuture<String> f = new CompletableFuture<>();

        db.collection("users")
                .document(userId)
                .collection("notifications")
                .add(n)
                .addOnSuccessListener(ref -> {
                    String id = ref.getId();
                    n.setId(ref.getId());              // if your model has setId(...)
                    f.complete(ref.getId());
                })
                .addOnFailureListener(f::completeExceptionally);

        return f;
    }


}

