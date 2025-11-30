package com.example.lotterypatentpending.exceptions;

/**
 * Exception for when a user should exist but doesn't
 */
public class UserNotFoundException extends RuntimeException {
    /**
     * Creates a UserNotFoundException
     * @param message error message
     */
    public UserNotFoundException(String message) {
        super(message);
    }
}
