package com.example.lotterypatentpending;

import android.util.Pair;

import java.util.ArrayList;

public class WaitingList {
    private ArrayList<Pair<Entrant, WaitingListState>> list;
    private Integer numSelect;

    public WaitingList(Integer numSelect) {
        list = new ArrayList<>();
        this.numSelect = numSelect;
    }

    public void addEntrant(Entrant entrant) {
        list.add(new Pair<Entrant, WaitingListState>(entrant, WaitingListState.ENTERED));
    }

    /**
     * Selects a number of people randomly with a lottery system
     */
    public void lotterySelect() {

    }

    /**
     * Reselects people, caring about states as well
     */
    public void lotteryReselect() {

    }

    public void setNumSelect(Integer numSelect) {
        this.numSelect = numSelect;
    }

    public Integer getNumSelect() {
        return this.numSelect;
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
