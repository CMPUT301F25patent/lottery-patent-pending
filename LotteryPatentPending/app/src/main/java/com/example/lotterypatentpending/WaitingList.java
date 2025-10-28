package com.example.lotterypatentpending;

import android.util.Pair;

import java.util.ArrayList;

public class WaitingList {
    private ArrayList<Pair<Entrant, WaitingListState>> list;

    public WaitingList() {
        list = new ArrayList<>();
    }

    public void addEntrant(Entrant entrant) {
        list.add(new Pair<Entrant, WaitingListState>(entrant, WaitingListState.ENTERED));
    }

    /**
     * Selects a number of people randomly with a lottery system
     */
    public void lotterySelect(Integer numSelect) {
        LotterySystem.lotterySelect(this.list, numSelect);
    }

    /**
     * Reselects people, caring about states as well
     */
    public void lotteryReselect(Integer numSelect) {
        LotterySystem.lotteryReselect(this.list, numSelect);
    }

    public Integer getNumEntrants() {
        Integer n = 0;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).second != WaitingListState.NOT_IN) {
                n++;
            }
        }
        return n;
    }

}
