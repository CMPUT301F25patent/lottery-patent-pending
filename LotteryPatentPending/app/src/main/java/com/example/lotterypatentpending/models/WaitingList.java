package com.example.lotterypatentpending.models;

import androidx.core.util.Pair;

import com.example.lotterypatentpending.exceptions.UserInListException;
import com.example.lotterypatentpending.exceptions.UserNotInListException;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a waiting list for an event that entrants can join.
 *
 * @author Michael Gao
 * @maintainer Michael Gao
 */
public class WaitingList {
    private ArrayList<Pair<User, WaitingListState>> list = new ArrayList<>();
    private int capacity = -1;

    /**
     * list is initialized at the attribute level, for Firebase safety purposes
     */
    public WaitingList() {
    }

    /**
     * list initialzed with a max capacity
     * @param capacity
     */
    public WaitingList(int capacity){
        super();
        this.capacity = capacity;
    }

    /**
     * Adds an entrant to the waiting list, only if the entrant is not already in the list.
     * @param entrant
     */
    public void addEntrant(User entrant) {
        boolean exists = false;
        if(list.size() == capacity){
            return; // TODO: throw runtime exception
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

    /**
     * gets the underlying list
     * @return
     */
    public ArrayList<Pair<User, WaitingListState>> getList() {
        return list;
    }

    /**
     * sets the underlying list
     * @param list
     */
    public void setList(ArrayList<Pair<User, WaitingListState>> list) {
        this.list = list;
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
     * Updates the stored state for the given entrant.
     *
     * @param entrant user whose state should change
     * @param state   new {@link WaitingListState}
     * @return true if the entrant was found and updated; false otherwise
     */
    public boolean updateEntrantState(User entrant, WaitingListState state) {
        int index = -1;
        for (int i = 0; i < this.list.size(); i++) {
            Pair<User, WaitingListState> pair = this.list.get(i);
            if (pair.first.getUserId() != null && pair.first.getUserId().equals(entrant.getUserId())) {
                index = i;
                break;
            }
        }

        if (index != -1) {
            Pair<User, WaitingListState> newPair = new Pair<>(entrant, state);
            this.list.remove(index);
            this.list.add(index, newPair);

            return true;
        }

        return false;
    }
    /** @return capacity limit; -1 means unlimited */
    public int getCapacity() {
        return capacity;
    }
    /** Sets the maximum number of entrants allowed. */
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    /**
     * Runs the lottery selection algorithm on the current waiting list.
     * Delegates to {@link LotterySystem#lotterySelect(List, Integer)}.
     *
     * @param numSelect number of entrants to select
     */
    public void lotterySelect(Integer numSelect) {
        LotterySystem.lotterySelect(this.list, numSelect);
    }

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
    /**
     * @return all users in the waiting list (states discarded)
     */
    public List<User> getUsersOnly(){
        ArrayList<User> users = new ArrayList<>();
        for (Pair<User, WaitingListState> pair : list) {
            users.add(pair.first);
        }

        return users;
    }
    /**
     * @return users whose state is {@code SELECTED}
     */
    public List<User> getSelectedEntrants() {
        List<User> selectedUsers = new ArrayList<>();

        for (Pair<User, WaitingListState> pair : this.list) {
            if (pair.second == WaitingListState.SELECTED) {
                selectedUsers.add(pair.first);
            }
        }

        return selectedUsers;
    }

    public String exportAcceptedEntrantsToCsv() {
        StringBuilder csvData = new StringBuilder();

        csvData.append("Name,Email,Phone\n");
        for (Pair<User, WaitingListState> pair : this.list) {
            if (pair.second == WaitingListState.ACCEPTED) {
                User user = pair.first;
                String name = (user.getName() != null) ? user.getName() : "";
                String email = (user.getEmail() != null) ? user.getEmail() : "";
                String phone = (user.getContactInfo() != null) ? user.getContactInfo() : "";

                csvData.append(name).append(",");
                csvData.append(email).append(",");
                csvData.append(phone).append(",");
                csvData.append("\n");
            }
        }

        return csvData.toString();
    }
}
