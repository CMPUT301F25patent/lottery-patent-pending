package com.example.lotterypatentpending.models;

/**
 * State of an entrant in the waiting list
 *
 * @author Michael Gao
 * @maintainer Michael Gao
 */
public enum WaitingListState {
    NOT_IN, // error state
    ENTERED,
    SELECTED,
    NOT_SELECTED,
    ACCEPTED,
    DECLINED,
    CANCELED,

}
