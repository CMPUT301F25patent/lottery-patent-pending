package com.example.lotterypatentpending.models;

import androidx.core.util.Pair;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * LotterySystem is the collection of functions used to randomly select certain entrants from a list of entrants.
 *
 * @author Michael Gao
 * @maintainer Michael Gao, Erik Bacsa
 */

public class LotterySystem {

    /**
     * Unified lottery:
     * - Picks winners from users whose state is ENTERED or NOT_SELECTED
     * - NOT_SELECTED is for redrawing the lottery
     * - Sets winners to SELECTED
     * - Sets everyone else in that group to NOT_SELECTED
     *
     * Other states (ACCEPTED, DECLINED, CANCELED, etc.) are NOT touched.
     */
    public static void lotteryDraw(List<Pair<User, WaitingListState>> list, int numSlots) {
        if (list == null || numSlots <= 0) {
            return;
        }

        // Indices of candidates: either ENTERED or NOT_SELECTED
        List<Integer> candidateIndices = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            Pair<User, WaitingListState> entry = list.get(i);
            WaitingListState state = entry.second;

            if (state == WaitingListState.ENTERED ||
                    state == WaitingListState.NOT_SELECTED) {
                candidateIndices.add(i);
            }
        }

        if (candidateIndices.isEmpty()) {
            return; // nobody eligible
        }

        // Shuffle candidates
        Collections.shuffle(candidateIndices);

        // Can't select more winners than candidates
        int toSelect = Math.min(numSlots, candidateIndices.size());

        // First 'toSelect' become SELECTED, rest become NOT_SELECTED
        for (int k = 0; k < candidateIndices.size(); k++) {
            int idx = candidateIndices.get(k);
            Pair<User, WaitingListState> oldPair = list.get(idx);

            WaitingListState newState =
                    (k < toSelect) ? WaitingListState.SELECTED : WaitingListState.NOT_SELECTED;

            list.set(idx, new Pair<>(oldPair.first, newState));
        }
    }
}
