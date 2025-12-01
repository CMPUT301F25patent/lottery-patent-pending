package com.example.lotterypatentpending.viewModels;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

import com.example.lotterypatentpending.models.LotteryResultNotifier;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.util.List;

/**
 * Thin ViewModel wrapper exposing "publish results" as a single Task.
 * Coordinates notifying winners and losers via LotteryResultNotifier.
 *
 * @author Moffat
 * @maintainer Moffat
 */
public class OrganizerViewModel extends ViewModel {

    private final LotteryResultNotifier resultNotifier = new LotteryResultNotifier();

    /**
     * Publish lottery results: notify winners AND losers.
     * Returns a Task<Void> that completes when both sub-tasks finish.
     */
    public Task<Void> publishResults(@NonNull String organizerId,
                                     @NonNull String eventId,
                                     @NonNull String eventTitle,
                                     @NonNull List<String> allEntrantIds,
                                     @NonNull List<String> winnerIds) {

        // Notify Winners (Category.LOTTERY_WIN)
        Task<Void> tWin = resultNotifier.notifyWinners(
                organizerId, eventId, eventTitle, winnerIds
        );

        // Notify Losers (Category.LOTTERY_LOSE)
        Task<Void> tLose = resultNotifier.notifyLosersFromPool(
                organizerId, eventId, eventTitle, allEntrantIds, winnerIds
        );

        // Completes successfully when BOTH finish, or fails if either fails
        return Tasks.whenAll(tWin, tLose);
    }
}
