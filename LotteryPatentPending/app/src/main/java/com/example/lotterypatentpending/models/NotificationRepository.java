package com.example.lotterypatentpending.models;

import com.google.firebase.firestore.ListenerRegistration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

//Abstraction over notification storage & live queries
public interface NotificationRepository {
    CompletableFuture<Void> add(Notification n);
    CompletableFuture<Void> markRead(String userId, String notificationId);
    CompletableFuture<List<Notification>> getForUser(String userId);

    ListenerRegistration listenUnreadCount(String userId,
                                           java.util.function.Consumer<Integer> onCount,
                                           java.util.function.Consumer<Exception> onError);
}
