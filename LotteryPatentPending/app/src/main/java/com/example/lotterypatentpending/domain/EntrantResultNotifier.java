package com.example.lotterypatentpending.domain;

import com.example.lotterypatentpending.models.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Sends a single WIN/LOSE notification for one entrant and logs the action.
 *
 * <p>Used during lottery publication when you need per-user confirmation.
 *
 * @author Moffat
 * @maintainer Moffat
 */
public class EntrantResultNotifier {
    private final NotificationRepository notifications;
    private final AdminLogRepository logs;

    public EntrantResultNotifier(NotificationRepository n, AdminLogRepository l){
        notifications=n; logs=l;
    }
    /**
     * Sends a WIN notification to a single user and writes an audit log entry.
     *
     * <p>Creates a {@link Notification} with category WIN, stores it via the
     * {@link NotificationRepository}, and then records the action in the
     * {@link AdminLogRepository}.</p>
     *
     * @param org   organizer user ID initiating the notification
     * @param evt   event ID
     * @param uid   recipient user ID
     * @param title notification title
     * @param body  notification body
     * @return CompletableFuture completing when both the write and log are done
     */
    public CompletableFuture<Void> notifyWin(String org,String evt,String uid,String title,String body){
        var n=new Notification(uid, evt, org, title, body, Notification.Category.LOTTERY_WIN);
        return notifications.add(n).thenCompose(v -> logs.record(new NotificationLog(
                org, evt, Notification.Category.LOTTERY_WIN, List.of(uid), shorten(body))));
    }
    /**
     * Sends a LOSE notification to a single user and writes an audit log entry.
     *
     * <p>Creates a {@link Notification} with category LOSE, stores it, and
     * then records an audit log for administrator visibility.</p>
     *
     * @param org   organizer user ID initiating the notification
     * @param evt   event ID
     * @param uid   recipient user ID
     * @param title notification title
     * @param body  notification body
     * @return CompletableFuture completing when both the write and log finish
     */
    public CompletableFuture<Void> notifyLose(String org,String evt,String uid,String title,String body){
        var n=new Notification(uid, evt, org, title, body, Notification.Category.LOTTERY_LOSE);
        return notifications.add(n).thenCompose(v -> logs.record(new NotificationLog(
                org, evt, Notification.Category.LOTTERY_LOSE, List.of(uid), shorten(body))));
    }
    /**
     * Truncates a body string to at most 100 characters for log previews.
     *
     * @param s the original message body
     * @return the truncated string if longer than 100 chars, otherwise the original
     */
    private static String shorten(String s){ return s.length()>100 ? s.substring(0,100) : s; }
}

