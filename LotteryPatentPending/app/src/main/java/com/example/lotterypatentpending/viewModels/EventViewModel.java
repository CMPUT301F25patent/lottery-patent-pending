package com.example.lotterypatentpending.viewModels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.lotterypatentpending.models.Event;

public class EventViewModel extends ViewModel {
    private final MutableLiveData<Event> selectedEvent = new MutableLiveData<>();

    public void setEvent(Event event) {
        selectedEvent.setValue(event);
    }

    public LiveData<Event> getEvent() {
        return selectedEvent;
    }
}
