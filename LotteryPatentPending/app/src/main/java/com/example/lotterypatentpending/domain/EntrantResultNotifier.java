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

    public CompletableFuture<Void> notifyWin(String org,String evt,String uid,String title,String body){
        var n=new Notification(uid, evt, org, title, body, Notification.Category.WIN);
        return notifications.add(n).thenCompose(v -> logs.record(new NotificationLog(
                org, evt, Notification.Category.WIN, List.of(uid), shorten(body))));
    }
    public CompletableFuture<Void> notifyLose(String org,String evt,String uid,String title,String body){
        var n=new Notification(uid, evt, org, title, body, Notification.Category.LOSE);
        return notifications.add(n).thenCompose(v -> logs.record(new NotificationLog(
                org, evt, Notification.Category.LOSE, List.of(uid), shorten(body))));
    }
    private static String shorten(String s){ return s.length()>100 ? s.substring(0,100) : s; }
}

