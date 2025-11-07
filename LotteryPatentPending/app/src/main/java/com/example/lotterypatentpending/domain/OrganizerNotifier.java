package com.example.lotterypatentpending.domain;

import com.example.lotterypatentpending.data.UserDataSource;
import com.example.lotterypatentpending.models.*;
import com.example.lotterypatentpending.models.Notification.Category;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Application service for organizer-triggered fan-outs (broadcasts).
 *
 * <p>Coordinates:
 * <ul>
 *   <li>look up entrant ids via {@link UserDataSource}</li>
 *   <li>filter by notifications opt-in</li>
 *   <li>fan-out {@link Notification}s via {@link NotificationRepository}</li>
 *   <li>write an {@link NotificationLog} for admin auditing</li>
 * </ul>
 *
 * @author Moffat
 * @maintainer Moffat
 */

public class OrganizerNotifier {
    private final NotificationRepository notifications;
    private final UserDataSource users;
    private final AdminLogRepository logs;

    public OrganizerNotifier(NotificationRepository n, UserDataSource u, AdminLogRepository l){
        notifications=n; users=u; logs=l;
    }

    /** Notify only the chosen users invited to sign up for an event. */
    public CompletableFuture<List<String>> notifyChosenToSignup(String org,String evt,String title,String body,List<String> chosenIds){
        return users.filterOptedIn(evt, chosenIds)
                .thenCompose(ids -> fanout(ids, u -> new Notification(u, evt, org, title, body, Category.CHOSEN_SIGNUP)))
                .thenCompose(ids -> log(org, evt, Category.CHOSEN_SIGNUP, ids, body).thenApply(v -> ids));
    }

    /** Broadcast to entire WAITLIST group (opt-in respected). */
    public CompletableFuture<List<String>> notifyAllWaitlist(String org,String evt,String title,String body){
        return notifyGroup(org, evt, title, body, UserDataSource.Group.WAITLIST, Category.WAITLIST);
    }

    /** Broadcast to entire SELECTED group (opt-in respected). */
    public CompletableFuture<List<String>> notifyAllSelected(String org,String evt,String title,String body){
        return notifyGroup(org, evt, title, body, UserDataSource.Group.SELECTED, Category.SELECTED);
    }

    /** Broadcast to entire CANCELLED group (opt-in respected). */
    public CompletableFuture<List<String>> notifyAllCancelled(String org,String evt,String title,String body){
        return notifyGroup(org, evt, title, body, UserDataSource.Group.CANCELLED, Category.CANCELLED);
    }

    private CompletableFuture<List<String>> notifyGroup(String org, String evt, String title, String body, UserDataSource.Group g, Category cat){
        return users.getEntrantIds(evt, g)
                .thenCompose(ids -> users.filterOptedIn(evt, ids))
                .thenCompose(ids -> fanout(ids, u -> new Notification(u, evt, org, title, body, cat)))
                .thenCompose(ids -> log(org, evt, cat, ids, body).thenApply(v -> ids));
    }
    private CompletableFuture<List<String>> fanout(List<String> userIds, Function<String,Notification> mk){
        var writes = userIds.stream().map(u -> notifications.add(mk.apply(u))).collect(Collectors.toList());
        return CompletableFuture.allOf(writes.toArray(new CompletableFuture[0])).thenApply(v -> userIds);
    }
    private CompletableFuture<Void> log(String org,String evt,Category c,List<String> ids,String body){
        String preview = body.length()>100 ? body.substring(0,100) : body;
        return logs.record(new NotificationLog(org, evt, c, ids, preview));
    }
}

