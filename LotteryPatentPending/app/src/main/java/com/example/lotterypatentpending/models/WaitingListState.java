package com.example.lotterypatentpending.models;

/**
 * State of an entrant in the waiting list
 *
 * @author Michael Gao
 * @maintainer Michael Gao
 */
public enum WaitingListState {
    NOT_IN, // error state
    ENTERED, //User has entered event
    SELECTED, //User has been selected for event but not accepted
    NOT_SELECTED, //User in waiting list but has not been selected
    ACCEPTED, //User has accepted event
    DECLINED, //User has declined event invitation
    CANCELED, //User has canceled an accepted invite

}
