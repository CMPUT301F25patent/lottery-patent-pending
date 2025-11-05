package com.example.lotterypatentpending.viewmodels;

import com.example.lotterypatentpending.models.User;

public class AppSession {
    private static String currentUid;
    private static User currentUser;

    public static void setCurrentUser(User user) {
        currentUser = user;
        currentUid = user != null ? user.getUserId() : null;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static String getCurrentUid() {
        return currentUid;
    }
}