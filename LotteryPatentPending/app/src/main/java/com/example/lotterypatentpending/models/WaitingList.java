package com.example.lotterypatentpending.models;

import android.util.Pair;

import com.example.lotterypatentpending.exceptions.UserInListException;
import com.example.lotterypatentpending.exceptions.UserNotInListException;

import java.util.ArrayList;
import java.util.List;

public class WaitingList {
    private List<User> entered = new ArrayList<>();
    private List<User> selected = new ArrayList<>();
    private List<User> notSelected = new ArrayList<>();
    private List<User> accepted = new ArrayList<>();
    private List<User> declined = new ArrayList<>();
    private List<User> cancelled = new ArrayList<>();

    public WaitingList() {    }

    public void addEntrant(User entrant) {
        if (!entered.contains(entrant)) {
            entered.add(entrant);
        }
        else {
            throw new UserInListException("User already in list.");
        }
    }

    public void removeEntrant(User entrant) {
        if (entered.contains(entrant)) {
            entered.remove(entrant);
        }
        else {
            throw new UserNotInListException("User not found in list.");
        }
    }

    public boolean checkEntrant(User entrant) {
        return (entered.contains(entrant) || selected.contains(entrant) || notSelected.contains(entrant) || declined.contains(entrant) || cancelled.contains(entrant));
    }

    /**
     * Selects a number of people randomly with a lottery system
     */
    public void lotterySelect(Integer numSelect) {
        Pair<List<User>, List<User>> pair = LotterySystem.lotterySelect(this.entered, numSelect);
        selected = pair.first;
        notSelected = pair.second;
    }

    /**
     * Reselects people, caring about states as well
     */
    public void lotteryReselect(Integer numSelect) {
    }

    public Integer getNumAll() {
        return entered.size() + selected.size() + notSelected.size() + accepted.size() + declined.size() + cancelled.size();
    }

}
