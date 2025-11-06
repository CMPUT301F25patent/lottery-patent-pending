package com.example.lotterypatentpending.data;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface UserDataSource {
    enum Group { WAITLIST, SELECTED, CANCELLED }
    CompletableFuture<List<String>> getEntrantIds(String eventId, Group group);
    CompletableFuture<List<String>> filterOptedIn(String eventId, List<String> candidateUserIds);
}

