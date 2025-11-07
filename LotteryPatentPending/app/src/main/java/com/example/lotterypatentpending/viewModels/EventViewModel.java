package com.example.lotterypatentpending.viewModels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.lotterypatentpending.models.Event;
import com.example.lotterypatentpending.models.FirebaseManager;

public class EventViewModel extends ViewModel {
    private final MutableLiveData<Event> selectedEvent = new MutableLiveData<>();
    private FirebaseManager fm = FirebaseManager.getInstance();

    public void setEvent(Event event) {
        selectedEvent.setValue(event);
    }

    public LiveData<Event> getEvent() {
        return selectedEvent;
    }

    public void updateGeoRequired(boolean required) {
        Event event = selectedEvent.getValue();
        if (event == null) return;

        // Update the local event object
        event.setGeolocationRequired(required);
        selectedEvent.setValue(event);

        // Update in Firestore
        fm.updateEventField("geolocationRequired", event, required);
    }
}
