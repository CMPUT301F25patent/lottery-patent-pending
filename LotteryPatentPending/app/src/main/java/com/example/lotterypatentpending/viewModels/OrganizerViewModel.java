package com.example.lotterypatentpending.viewModels;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

import com.example.lotterypatentpending.models.LotteryResultNotifier;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.util.List;

/**
 * Holds the “publish results” logic so the Activity stays thin.
 * Returns Firebase Tasks so Activities can add success/failure listeners.
 */
public class OrganizerViewModel extends ViewModel {

    private final LotteryResultNotifier resultNotifier = new LotteryResultNotifier();

    /**
     * Publish lottery results: notify winners AND losers.
     * Returns a Task that completes when both sub-tasks finish.
     */
    public Task<List<Task<?>>> publishResults(@NonNull String organizerId,
                                              @NonNull String eventId,
                                              @NonNull String eventTitle,
                                              @NonNull List<String> allEntrantIds,
                                              @NonNull List<String> winnerIds) {
        Task<Void> tWin  = resultNotifier.notifyWinners(organizerId, eventId, eventTitle, winnerIds);
        Task<Void> tLose = resultNotifier.notifyLosersFromPool(organizerId, eventId, eventTitle, allEntrantIds, winnerIds);
        // whenAllComplete lets the Activity re-enable UI / show a single toast afterward
        return Tasks.whenAllComplete(tWin, tLose);
    }
}

