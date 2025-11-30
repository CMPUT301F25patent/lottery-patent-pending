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

    /**
     * Creates an EntrantResultNotifier
     * @param n NotificationRepository
     * @param l AdminLogRepository
     */
    public EntrantResultNotifier(NotificationRepository n, AdminLogRepository l){
        notifications=n; logs=l;
    }

    /**
     * Notifies entrants if they were selected
     * @param org Organizer ID
     * @param evt Event ID
     * @param uid User ID
     * @param title notif title
     * @param body notif body
     * @return a completable future that gets completed if the call worked
     */
    public CompletableFuture<Void> notifyWin(String org,String evt,String uid,String title,String body){
        var n=new Notification(uid, evt, org, title, body, Notification.Category.WIN);
        return notifications.add(n).thenCompose(v -> logs.record(new NotificationLog(
                org, evt, Notification.Category.WIN, List.of(uid), shorten(body))));
    }

    /**
     * Notifies entrants if they were not selected
     * @param org Organizer ID
     * @param evt Event ID
     * @param uid User ID
     * @param title notif title
     * @param body notif body
     * @return a completable future that gets completed if the call worked
     */
    public CompletableFuture<Void> notifyLose(String org,String evt,String uid,String title,String body){
        var n=new Notification(uid, evt, org, title, body, Notification.Category.LOSE);
        return notifications.add(n).thenCompose(v -> logs.record(new NotificationLog(
                org, evt, Notification.Category.LOSE, List.of(uid), shorten(body))));
    }

    /**
     * truncates a log to 100 characters
     * @param s input string
     * @return truncated string
     */
    private static String shorten(String s){ return s.length()>100 ? s.substring(0,100) : s; }
}

