package com.example.lotterypatentpending.models;

import androidx.core.util.Pair;

import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * LotterySystem is the collection of functions used to randomly select certain entrants from a list of entrants.
 *
 * @author Michael Gao
 * @maintainer Michael Gao
 */
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
}
