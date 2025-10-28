package com.example.lotterypatentpending;

import android.util.Pair;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class LotterySystem {
    public static void lotterySelect(List<Pair<Entrant, WaitingListState>> list, Integer num) {
        Collections.shuffle(list);
        for (int i = 0; i < list.size(); i++) {
            Pair<Entrant, WaitingListState> pair = list.get(i);
            WaitingListState newState = (i < num) ? WaitingListState.SELECTED : WaitingListState.NOT_SELECTED;
            list.set(i, new Pair<>(pair.first, newState));
        }
        list.sort(Comparator.comparing(pair -> pair.first.getName()));
    }

    public static void lotteryReselect() {

    }
}
