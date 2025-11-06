package com.example.lotterypatentpending.models;

import android.util.Pair;

import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LotterySystem {
    /**
     * Selects random entrants by shuffling the list and picking the first n entrants, then sorting the list
     * @param list list of entrants
     * @param num number of entrants to select
     */
    public static void lotterySelect(List<Pair<User, WaitingListState>> list, Integer num) {
        Collections.shuffle(list);
        for (int i = 0; i < list.size(); i++) {
            Pair<User, WaitingListState> pair = list.get(i);
            WaitingListState newState = (i < num) ? WaitingListState.SELECTED : WaitingListState.NOT_SELECTED;
            list.set(i, new Pair<>(pair.first, newState));
        }
    }

    private void notifyLotteryResults(String organizerId,
                                      String eventId,
                                      String eventTitle,
                                      List<String> allEntrantIds,   // << full pool considered by the draw
                                      List<String> winnerIds) {

        // Our facade (handles opt-in filtering + admin audit logging)
        LotteryResultNotifier notifier = new LotteryResultNotifier();

        List<Task<Void>> tasks = new ArrayList<>();

        // 1) Winners
        if (winnerIds != null && !winnerIds.isEmpty()) {
            tasks.add(notifier.notifyWinners(organizerId, eventId, eventTitle, winnerIds));
        }

        // 2) Losers: compute from the pool (server will filter opt-in)
        List<String> safeWinners = (winnerIds != null) ? winnerIds : Collections.emptyList();
        if (allEntrantIds != null && !allEntrantIds.isEmpty()) {
            tasks.add(notifier.notifyLosersFromPool(
                    organizerId, eventId, eventTitle, allEntrantIds, safeWinners));
        }
    }
    /*
    public static void lotteryReselect(List<Pair<User, WaitingListState>> list, Integer num){

    }
    */
}
