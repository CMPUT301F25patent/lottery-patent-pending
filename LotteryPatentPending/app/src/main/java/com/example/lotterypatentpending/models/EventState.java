package com.example.lotterypatentpending.models;

/**
 * Represents the stages that an event can be in
 */
public enum EventState {
    NOT_STARTED,
    OPEN_FOR_REG,
    CLOSED_FOR_REG,
    SELECTED_ENTRANTS,
    CONFIRMED_ENTRANTS,
    ENDED,
    CANCELLED
}
