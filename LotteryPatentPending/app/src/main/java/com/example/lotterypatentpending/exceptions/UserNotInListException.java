package com.example.lotterypatentpending.exceptions;

/**
 * Exception for when a user should be in a list but isn't
 */
public class UserNotInListException extends RuntimeException {
    /**
     * Creates a UserNotInListException
     * @param message error message
     */
    public UserNotInListException(String message) {
        super(message);
    }
}
