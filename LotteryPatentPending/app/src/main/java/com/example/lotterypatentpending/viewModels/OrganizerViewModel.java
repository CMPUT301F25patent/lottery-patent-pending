package com.example.lotterypatentpending.viewModels;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

import com.example.lotterypatentpending.models.LotteryResultNotifier;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.util.List;

/**
 * Thin ViewModel wrapper exposing "publish results" as a single Task.
 *
 * <p>UI (Activity/Fragment) observes the task to disable buttons, show toasts,
 * and handle errors without holding domain logic.
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

        Task<Void> tWin  = resultNotifier.notifyWinners(
                organizerId, eventId, eventTitle, winnerIds
        );

        Task<Void> tLose = resultNotifier.notifyLosersFromPool(
                organizerId, eventId, eventTitle, allEntrantIds, winnerIds
        );

        // Completes successfully when BOTH finish, or fails if either fails
        return Tasks.whenAll(tWin, tLose);
    }
}
