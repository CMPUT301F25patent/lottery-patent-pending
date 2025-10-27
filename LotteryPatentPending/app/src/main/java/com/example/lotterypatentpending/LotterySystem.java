package com.example.lotterypatentpending;

import android.util.Pair;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class LotterySystem {
    public static void selectEntrants(List<Pair<Entrant, WaitingListState>> entrants, Integer n) {
        Collections.shuffle(entrants);
        for (int i = 0; i < entrants.size(); i++) {
            Pair<Entrant, WaitingListState> pair = entrants.get(i);
            WaitingListState newState = (i < n) ? WaitingListState.ENTERED : WaitingListState.CANCELED;
            entrants.set(i, new Pair<>(pair.first, newState));
        }
        entrants.sort(Comparator.comparing(pair -> pair.first.getName()));
    }

    public static void reselectEntrants() {

    }
}
