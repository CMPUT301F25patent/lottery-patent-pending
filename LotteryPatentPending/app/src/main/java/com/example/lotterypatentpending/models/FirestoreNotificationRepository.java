package com.example.lotterypatentpending.models;

import com.google.firebase.firestore.*;
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

    @Override public CompletableFuture<Void> add(Notification n){
        var f=new CompletableFuture<Void>();
        db.collection("users").document(n.getUserId()).collection("notifications")
                .add(n).addOnSuccessListener(r->f.complete(null)).addOnFailureListener(f::completeExceptionally);
        return f;
    }
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

}

