package com.example.lotterypatentpending.viewModels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.lotterypatentpending.models.Event;
import com.example.lotterypatentpending.models.FirebaseManager;

/**
 * ViewModel for managing the currently selected Event.
 * <p>
 * Holds a MutableLiveData<Event> that can be observed by fragments to react
 * to changes in the selected event. Also provides a method to update the
 * geolocationRequired field in both local LiveData and Firestore.
 * </p>
 */
public class EventViewModel extends ViewModel {

    /** LiveData holding the currently selected event. */
    private final MutableLiveData<Event> selectedEvent = new MutableLiveData<>();

    /** Instance of FirebaseManager to handle Firestore updates. */
    private FirebaseManager fm = FirebaseManager.getInstance();

    /**
     * Sets the currently selected event.
     *
     * @param event The Event object to be set as the selected event.
     */
    public void setEvent(Event event) {
        selectedEvent.setValue(event);
    }

    /**
     * Returns a LiveData object representing the currently selected event.
     * Fragments can observe this LiveData to update UI when the event changes.
     *
     * @return LiveData<Event> representing the selected event.
     */
    public LiveData<Event> getEvent() {
        return selectedEvent;
    }

    /**
     * Updates the geolocationRequired field of the currently selected event.
     * <p>
     * This method updates both the local LiveData object and the Firestore database
     * via FirebaseManager.
     * </p>
     *
     * @param required True if geolocation is required for the event, false otherwise.
     */
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
