package com.example.lotterypatentpending.models;

public class RecipientRef {
    private String userId;

    public RecipientRef(String userId){
        this.userId = userId;
    }

    //Getter
    public String getUserId(){
        return userId;
    }

    //Setter

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
