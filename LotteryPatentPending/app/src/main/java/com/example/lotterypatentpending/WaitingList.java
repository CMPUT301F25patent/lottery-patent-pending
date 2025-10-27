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
     * @param num number of people to select
     */
    public void lotterySelect(Integer num) {

    }

    /**
     * Reselects people, caring about states as well
     * @param num
     */
    public void lotteryReselect(Integer num) {

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
