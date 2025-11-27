package com.example.lotterypatentpending.models;

import com.google.firebase.firestore.ListenerRegistration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Abstraction over notification storage & live queries.
 *
 * <p>Implementations (e.g., Firestore) persist notifications under
 * users/{userId}/notifications and support lightweight badge updates.
 *
 * @author Moffat
 * @maintainer Moffat
 */
public interface NotificationRepository {
    /**
     * Persist a new notification under {@code n.getUserId()}.
     * @return future that completes when the write is committed
     */
    CompletableFuture<Void> add(Notification n);

    /**
     * Mark an existing notification as read.
     * @param userId owner of the notifications subcollection
     * @param notificationId Firestore document id
     */
    CompletableFuture<Void> markRead(String userId, String notificationId);

    /**
     * Fetch all notifications for a user, newest first.
     */
    CompletableFuture<List<Notification>> getForUser(String userId);

    /**
     * Observe unread count for a user. Call {@link ListenerRegistration#remove()}
     * when the screen is stopped.
     */
    ListenerRegistration listenUnreadCount(String userId,
                                           java.util.function.Consumer<Integer> onCount,
                                           java.util.function.Consumer<Exception> onError);
    interface NotificationsListener {
        void onChanged(List<Notification> notifications);
        void onError(Exception e);
    }
    ListenerRegistration listenUserNotifications(String userId,
                                                 NotificationsListener listener);
    interface UnreadCountListener {
        void onChanged(long count);
    }

    interface ErrorListener {
        void onError(Exception e);
    }
}
