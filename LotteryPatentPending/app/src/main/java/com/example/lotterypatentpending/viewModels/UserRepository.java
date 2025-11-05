package com.example.lotterypatentpending.viewModels;

import com.example.lotterypatentpending.models.User;

public class UserRepository {
    private static UserRepository instance;
    private User currentUser;

    private UserRepository() {}

    public static synchronized UserRepository getInstance() {
        if (instance == null) {
            instance = new UserRepository();
        }
        return instance;
    }

    public User getUser() { return currentUser; }
    public void setUser(User u) { currentUser = u; }
}