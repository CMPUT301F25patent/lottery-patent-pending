package com.example.lotterypatentpending.exceptions;

/**
 * Exception for when a user should not be in a list, but is in a list
 */
public class UserInListException extends RuntimeException {
    /**
     * Creates a UserInListException
     * @param message error message
     */
    public UserInListException(String message) {
        super(message);
    }
}
