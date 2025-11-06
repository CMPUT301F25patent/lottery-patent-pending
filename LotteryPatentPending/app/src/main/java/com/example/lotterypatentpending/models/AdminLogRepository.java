package com.example.lotterypatentpending.models;

import java.util.concurrent.CompletableFuture;

public interface AdminLogRepository {
    CompletableFuture<Void> record(NotificationLog log);
}

