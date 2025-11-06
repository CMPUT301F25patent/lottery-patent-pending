package com.example.lotterypatentpending.models;

import android.util.Pair;
import com.example.lotterypatentpending.models.Notification;
import com.example.lotterypatentpending.models.NotificationRepository;
import com.example.lotterypatentpending.models.LotteryResultNotifier;
import com.example.lotterypatentpending.models.NotificationFactory;
import android.content.Context;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class LotterySystem {
    /**
     * Selects random entrants by shuffling the list and picking the first n entrants, then sorting the list
     * @param list list of entrants
     * @param num number of entrants to select
     */
    public static Pair<List<User>, List<User>> lotterySelect(List<User> list, Integer num) {
        Collections.shuffle(list);
        int selectBound = Math.min(num, list.size());
        List<User> selected = new ArrayList<>(list.subList(0, selectBound));
        List<User> notSelected = new ArrayList<>(list.subList(selectBound, list.size()));
        return new Pair<>(selected, notSelected);
    }

    private void notifyLotteryResults(String organizerId, String eventId, String eventTitle,
                                      List<String> winnerIds, List<String> loserIds) {
        NotificationRepository nRepo = new NotificationRepository();
        LotteryResultNotifier notifier = new LotteryResultNotifier(nRepo);

        List<com.google.android.gms.tasks.Task<Void>> tasks = new java.util.ArrayList<>();

        if (winnerIds != null && !winnerIds.isEmpty()) {
            tasks.add(notifier.notifyWinners(organizerId, eventId, eventTitle, winnerIds));
        }
        if (loserIds != null && !loserIds.isEmpty()) {
            tasks.add(notifier.notifyLosers(organizerId, eventId, eventTitle, loserIds));
        }
        com.google.android.gms.tasks.Tasks.whenAllComplete(tasks)
                .addOnSuccessListener(results -> {
                    int ok = 0, fail = 0;
                    for (com.google.android.gms.tasks.Task<?> t : results) {
                        if (t.isSuccessful()) ok++; else fail++;
                    }
                    if (fail != 0) {
                        android.util.Log.e("LotteryNotify", "Some notifications failed (" + fail + ")");
                    }
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("LotteryNotify", "Failed to send notifications", e);
                });
    }

    public static void lotteryReselect(List<User> list, Integer num){

    }

}
