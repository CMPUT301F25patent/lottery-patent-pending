package com.example.lotterypatentpending.viewModels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.lotterypatentpending.models.Event;
import com.example.lotterypatentpending.models.User;

/**
 * A global singleton for holding the user's current session, and an event so it's easier to get the current event a user is looking at
 *
 * @author Michael Gao
 * @maintainer Michael Gao
 */
public class UserEventRepository {
    private static UserEventRepository instance;
    private final MutableLiveData<User> user = new MutableLiveData<>();
    private final MutableLiveData<Event> event = new MutableLiveData<>();

    /**
     * Empty constructor, used for getInstance()
     */
    private UserEventRepository() {

    }

    /**
     * Returns a singleton of UserEventRepository
     * @return UserEventRepository singleton
     */
    public static synchronized UserEventRepository getInstance() {
        if (instance == null) {
            instance = new UserEventRepository();
        }
        return instance;
    }

    /**
     * Sets global user
     * @param user
     */
    public void setUser(User user) {
        this.user.postValue(user);
    }

    /**
     * Gets global user
     * @return
     */
    public LiveData<User> getUser() {
        return this.user;
    }

    /**
     * Sets global event
     * @param event
     */
    public void setEvent(Event event) {
        this.event.postValue(event);
    }

    /**
     * Gets global event
     * @return
     */
    public LiveData<Event> getEvent() {
        return this.event;
    }

}
