package com.example.lotterypatentpending.models;

import android.util.Pair;

import com.example.lotterypatentpending.exceptions.UserInListException;
import com.example.lotterypatentpending.exceptions.UserNotInListException;

import java.util.ArrayList;

public class WaitingList {
    private ArrayList<Pair<User, WaitingListState>> list;
    private int capacity;

    public WaitingList() {
        list = new ArrayList<>();
    }

    public WaitingList(int capacity){
        super();
        this.capacity = capacity;
    }

    public void addEntrant(User entrant) {
        boolean exists = false;
        if(list.size() == capacity){
            return;
        }
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

    public ArrayList<Pair<User, WaitingListState>> getList() {
        return list;
    }

    public void setList(ArrayList<Pair<User, WaitingListState>> list) {
        this.list = list;
    }

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

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
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
