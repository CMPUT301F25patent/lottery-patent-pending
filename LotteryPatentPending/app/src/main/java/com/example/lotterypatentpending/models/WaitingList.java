package com.example.lotterypatentpending.models;

import androidx.core.util.Pair;

import com.example.lotterypatentpending.exceptions.UserInListException;
import com.example.lotterypatentpending.exceptions.UserNotInListException;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a waiting list for an event that entrants can join.
 * It stores entrants as a pair of a {@link User} object and their current {@link WaitingListState}.
 *
 * @author Michael Gao
 * @maintainer Michael Gao
 */
public class WaitingList {
    /** The underlying list of entrants, stored as a pair of User and their state. */
    private ArrayList<Pair<User, WaitingListState>> list = new ArrayList<>();
    /** The maximum number of entrants allowed on the list. -1 indicates no limit. */
    private int capacity = -1;

    /**
     * No-argument constructor. The internal list is initialized at the attribute level for safety.
     */
    public WaitingList() {
    }

    /**
     * Constructs a new WaitingList with a specified maximum capacity.
     * @param capacity The maximum number of entrants allowed.
     */
    public WaitingList(int capacity){
        super();
        this.capacity = capacity;
    }

    /**
     * Adds an entrant to the waiting list.
     * <p>The entrant is added only if they are not already in the list and the list capacity has not been reached.
     * If the user already exists, a {@link UserInListException} is thrown.
     * @param entrant The {@link User} to add.
     * @throws UserInListException if the user is already present in the list.
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
     * Gets the underlying list of user/state pairs.
     * @return The {@code ArrayList} of {@code Pair<User, WaitingListState>}.
     */
    public ArrayList<Pair<User, WaitingListState>> getList() {
        return list;
    }

    /**
     * Sets the underlying list of user/state pairs.
     * @param list The new {@code ArrayList} to set.
     */
    public void setList(ArrayList<Pair<User, WaitingListState>> list) {
        this.list = list;
    }

    /**
     * Removes an entrant from the waiting list.
     * @param entrant The {@link User} to remove.
     * @throws UserNotInListException if the user is not found in the list.
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
     * Checks if an entrant is currently in the list.
     * @param entrant The {@link User} to check.
     * @return {@code true} if the entrant is in the list, {@code false} otherwise.
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
     * Updates the {@link WaitingListState} for a specific entrant.
     * @param entrant The {@link User} whose state needs updating.
     * @param state The new {@link WaitingListState} to assign.
     * @return {@code true} if the entrant was found and updated, {@code false} otherwise.
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

    /**
     * Gets the maximum number of entrants allowed on the list.
     * @return The capacity, or -1 if unlimited.
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * Sets the maximum number of entrants allowed on the list.
     * @param capacity The new capacity.
     */
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    /**
     * Randomly selects a specified number of entrants from the list using a lottery system.
     * The state of the selected users is updated by the {@link LotterySystem} class.
     * @param numSelect The number of entrants to select.
     */
    public void lotterySelect(Integer numSelect) {
        LotterySystem.lotterySelect(this.list, numSelect);
    }

    /**
     * Gets the total number of entrants in the list whose state is not {@link WaitingListState#NOT_IN}.
     * @return The total number of valid entrants.
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
     * Extracts only the {@link User} objects from the list of pairs.
     * @return A {@code List} of {@link User} objects.
     */
    public List<User> getUsersOnly(){
        ArrayList<User> users = new ArrayList<>();
        for (Pair<User, WaitingListState> pair : list) {
            users.add(pair.first);
        }

        return users;
    }

    /**
     * Gets a list of entrants whose current state is {@link WaitingListState#SELECTED}.
     * @return A {@code List} of selected {@link User} objects.
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
}