package com.example.lotterypatentpending.models;

/**
 * State of an entrant in the waiting list
 *
 * @author Michael Gao
 * @maintainer Michael Gao, Erik Bacsa
 */
public enum WaitingListState {
    NOT_IN, // error state


    //Both ENTERED AND NOT_SELECTED have same values for lottery system since users can be redrawn
    ENTERED, //User has entered event
    NOT_SELECTED, //User in waiting list but has not been selected CAN BE redrawn


    SELECTED, //User has been selected for event but not accepted the invite yet
    ACCEPTED, //User has accepted event and in the event


    DECLINED, //User has declined event invitation but can renter for lottery or leave
    CANCELED, //Organizer cancelled user for not accepting fast enough
}
