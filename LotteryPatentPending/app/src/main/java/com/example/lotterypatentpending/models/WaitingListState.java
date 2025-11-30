package com.example.lotterypatentpending.models;

/**
 * State of an entrant in the waiting list
 *
 * @author Michael Gao
 * @maintainer Michael Gao, Erik Bacsa
 */
public enum WaitingListState {
    NOT_IN, // error state

    //For lottery system entered and not_selected have same value
    ENTERED, //User has entered event
    NOT_SELECTED, //User in waiting list but has not been selected


    SELECTED, //User has been selected for event but not accepted
    ACCEPTED, //User has accepted event
    DECLINED, //User has declined event invitation
    CANCELED, //User or organizer has canceled the event invite

}
