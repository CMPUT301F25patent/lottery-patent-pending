package com.example.lotterypatentpending.models;

import com.example.lotterypatentpending.models.Notification;
import com.example.lotterypatentpending.models.NotificationFactory;
import com.example.lotterypatentpending.models.NotificationRepository;
import com.google.android.gms.tasks.Task;

import java.util.List;

public final class LotteryResultNotifier {

    private final NotificationRepository repo;

    public LotteryResultNotifier(NotificationRepository repo) {
        this.repo = repo;
    }

    public Task<Void> notifyWinners(String organizerId, String eventId, String eventTitle,
                                    List<String> winnerUserIds) {
        Notification n = NotificationFactory.lotteryWin(eventId, eventTitle, organizerId, winnerUserIds);
        return repo.createAndFanOut(n);
    }

    public Task<Void> notifyLosers(String organizerId, String eventId, String eventTitle,
                                   List<String> loserUserIds) {
        Notification n = NotificationFactory.lotteryLose(eventId, eventTitle, organizerId, loserUserIds);
        return repo.createAndFanOut(n);
    }
}

