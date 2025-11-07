package com.example.lotterypatentpending.models;

import java.util.concurrent.CompletableFuture;

/** Persists {@link NotificationLog} rows for admin auditing.
 * @author Moffat
 * @maintainer Moffat
 */

public interface AdminLogRepository {
    CompletableFuture<Void> record(NotificationLog log);
}

