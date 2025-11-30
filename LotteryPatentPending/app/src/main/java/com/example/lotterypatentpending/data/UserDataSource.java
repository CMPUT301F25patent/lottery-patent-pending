package com.example.lotterypatentpending.data;

import java.util.List;
import java.util.concurrent.CompletableFuture;
/**
 * Read-only access to user/entrant ids for an event and
 * filtering by "notifications opt-in".
 *
 * @author Moffat
 * @maintainer Moffat
 *
 * Used by domain services to decide who to notify.
 */
public interface UserDataSource {
    enum Group { WAITLIST, SELECTED, CANCELLED }
    CompletableFuture<List<String>> getEntrantIds(String eventId, Group group);
    CompletableFuture<List<String>> filterOptedIn(String eventId, List<String> candidateUserIds);
}

