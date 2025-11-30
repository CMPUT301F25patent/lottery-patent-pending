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
     * Persist a new notification document to the storage system, placing it under
     * the path corresponding to the notification's recipient ID ({@code n.getUserId()}).
     * @param n The {@link Notification} object to be saved.
     * @return A {@link CompletableFuture} that completes successfully when the write operation is committed, or completes exceptionally if the write fails.
     */
    CompletableFuture<Void> add(Notification n);

    /**
     * Marks an existing notification document as read in the storage system.
     * @param userId The ID of the user whose notifications subcollection contains the document.
     * @param notificationId The Firestore document ID of the specific notification to update.
     * @return A {@link CompletableFuture} that completes when the update operation is committed.
     */
    CompletableFuture<Void> markRead(String userId, String notificationId);

    /**
     * Asynchronously fetches all notifications for a specific user from the storage system.
     * The results should be ordered with the newest notifications appearing first (descending by creation time).
     * @param userId The ID of the user whose notifications are to be retrieved.
     * @return A {@link CompletableFuture} that returns a {@link List} of {@link Notification} objects on success.
     */
    CompletableFuture<List<Notification>> getForUser(String userId);

    /**
     * Establishes a real-time listener to observe the count of unread notifications for a specific user.
     * The listener will be triggered immediately and every time the unread count changes.
     * @param userId The ID of the user whose unread count is being monitored.
     * @param onCount A consumer function that accepts the new unread count (an {@link Integer}).
     * @param onError A consumer function that accepts an {@link Exception} if the real-time query fails.
     * @return A {@link ListenerRegistration} object. Call {@link ListenerRegistration#remove()} to stop listening for updates.
     */
    ListenerRegistration listenUnreadCount(String userId,
                                           java.util.function.Consumer<Integer> onCount,
                                           java.util.function.Consumer<Exception> onError);

    /**
     * Listener interface to handle real-time changes to a list of notifications.
     */
    interface NotificationsListener {
        /**
         * Called when the list of notifications changes or is initially loaded.
         * @param notifications The current list of notifications for the user.
         */
        void onChanged(List<Notification> notifications);

        /**
         * Called if an error occurs while listening for changes.
         * @param e The exception that occurred.
         */
        void onError(Exception e);
    }

    /**
     * Establishes a real-time listener to observe all notifications for a user, typically ordered newest first.
     * @param userId The ID of the user whose notification stream is being monitored.
     * @param listener The {@link NotificationsListener} to handle updates and errors.
     * @return A {@link ListenerRegistration} object. Call {@link ListenerRegistration#remove()} to stop listening.
     */
    ListenerRegistration listenUserNotifications(String userId,
                                                 NotificationsListener listener);

    /**
     * Listener interface for observing the unread count.
     * @deprecated Replaced by {@link java.util.function.Consumer} in the {@link #listenUnreadCount(String, java.util.function.Consumer, java.util.function.Consumer)} method signature.
     */
    interface UnreadCountListener {
        /**
         * Called when the unread count changes.
         * @param count The new unread notification count.
         */
        void onChanged(long count);
    }

    /**
     * General listener interface for observing errors.
     * @deprecated Error handling is now integrated into method signatures or specific listeners.
     */
    interface ErrorListener {
        /**
         * Called when a background operation or listener encounters an error.
         * @param e The exception that occurred.
         */
        void onError(Exception e);
    }
}