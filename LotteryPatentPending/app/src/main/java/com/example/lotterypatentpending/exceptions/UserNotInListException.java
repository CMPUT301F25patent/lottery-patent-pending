package com.example.lotterypatentpending.exceptions;

public class UserNotInListException extends RuntimeException {
    public UserNotInListException(String message) {
        super(message);
    }
}
