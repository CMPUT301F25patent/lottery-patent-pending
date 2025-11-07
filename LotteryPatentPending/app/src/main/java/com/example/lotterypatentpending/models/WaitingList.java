package com.example.lotterypatentpending.models;

import androidx.core.util.Pair;

import com.example.lotterypatentpending.exceptions.UserInListException;
import com.example.lotterypatentpending.exceptions.UserNotInListException;

import java.util.ArrayList;

/**
 * This class represents a waiting list for an event that entrants can join.
 *
 * @author Michael Gao
 * @maintainer Michael Gao
 */
public class WaitingList {
    private ArrayList<Pair<User, WaitingListState>> list = new ArrayList<>();

    /**
     * list is initialized at the attribute level, for Firebase safety purposes
     */
    public WaitingList() {    }

    /**
     * Adds an entrant to the waiting list, only if the entrant is not already in the list.
     * @param entrant
     */
    public void addEntrant(User entrant) {
        boolean exists = false;
        for (Pair<User, WaitingListState> pair : this.list) {
            if (pair.first.equals(entrant)) {
                exists = true;
                break;
            }
        }
        if (!exists) {
            this.list.add(new Pair<User, WaitingListState>(entrant, WaitingListState.ENTERED));
        }
        else {
            throw new UserInListException("User already in list.");
        }
    }

    /**
     * Removes an entrant from the waiting list
     * @param entrant
     */
    public void removeEntrant(User entrant) {
        boolean removed = false;
        for (int i = 0; i < this.list.size(); i++) {
            if (this.list.get(i).first.equals(entrant)) {
                removed = true;
                this.list.remove(i);
                break;
            }
        }
        if (!removed) {
            throw new UserNotInListException("User not found in list.");
        }
    }

    /**
     * Checks if an entrant is in the list
     * @param entrant
     * @return true if the entrant is in the class, false otherwise
     */
    public boolean checkEntrant(User entrant) {
        boolean in = false;
        for (Pair<User, WaitingListState> pair : this.list) {
            if (pair.first.equals(entrant)) {
                in = true;
                break;
            }
        }
        return in;
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
    /*
    public void lotteryReselect(Integer numSelect) {
        LotterySystem.lotteryReselect(this.list, numSelect);
    }

     */

    /**
     * Gets the number of all entrants in the list
     * @return number of entrants
     */
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
