package com.example.lotterypatentpending.models;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class UserEventRepository {
//    public static User getUser;
    private static UserEventRepository instance;
    private final MutableLiveData<User> user = new MutableLiveData<>();
    private final MutableLiveData<Event> event = new MutableLiveData<>();

    private UserEventRepository() {

    }

    public static synchronized UserEventRepository getInstance() {
        if (instance == null) {
            instance = new UserEventRepository();
        }
        return instance;
    }

    public void setUser(User user) {
        this.user.setValue(user);
    }

    public LiveData<User> getUser() {
        return this.user;
    }

    public void setEvent(Event event) {
        this.event.setValue(event);
    }

    public LiveData<Event> getEvent() {
        return this.event;
    }

    public void joinEvent() {


        User currentUser = user.getValue();
        Event currentEvent = event.getValue();

        if (currentUser != null && currentEvent != null) {
            FirebaseManager fm = FirebaseManager.getInstance();
            fm.addJoinedEventToEntrant(currentEvent, currentUser.getUserId());
            fm.addEntrantToWaitingList(currentUser, WaitingListState.ENTERED, currentEvent.getId());

            currentEvent.addToWaitingList(currentUser);
            currentUser.addJoinedEvent(currentEvent.getId());

            user.setValue(currentUser);
            event.setValue(currentEvent);


        }
    }
}
