package com.example.lotterypatentpending.data;

import com.example.lotterypatentpending.models.WaitingListState;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface UserDataSource {
    // Defines the notification groups
    enum Group { WAITLIST, SELECTED, CANCELLED, ATTENDING }

    CompletableFuture<List<String>> getEntrantsByState(String eventId, WaitingListState state);
    CompletableFuture<List<String>> getEntrantIds(String eventId, Group group);
    CompletableFuture<List<String>> filterOptedIn(String eventId, List<String> candidateUserIds);
}