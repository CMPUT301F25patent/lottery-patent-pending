package com.example.lotterypatentpending.models;
/**
 * Lightweight reference to a user who should receive a notification.
 * Stored inside Notification.recipients for fan-out messaging.
 */
public class RecipientRef {
    private String userId;
    /** Creates a reference to a user by UID. */
    public RecipientRef(String userId){
        this.userId = userId;
    }

    /** @return the recipient's user ID */
    public String getUserId(){
        return userId;
    }

    /** Sets the referenced user ID. */
    public void setUserId(String userId) {
        this.userId = userId;
    }
}
