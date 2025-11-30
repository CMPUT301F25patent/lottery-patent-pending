package com.example.lotterypatentpending.models;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

/**
 * Represents an Event in the system.
 * <p>
 * Holds all information related to an event, including title, description, date, capacity,
 * location, organizer, waiting list, selected entrants, and registration periods.
 * </p>
 * <p>
 * Provides utility methods to manage waiting list entries, check event activity status,
 * and handle geolocation requirements.
 * </p>
 *
 * @author Ebuka
 * @maintainer Ebuka
 * @contributor Erik
 */
public class Event {
    private String id;
    private String title;

    private String tag;
    private String description;
    private int capacity;
    private int waitingListCapacity;
    private String location;
    private WaitingList waitingList;
    private User organizer;
    private Timestamp date;
    private Timestamp regStartDate;
    private Timestamp regEndDate;
    private EventState eventState;
    private boolean geolocationRequired;
    private byte[] posterBytes; // compressed JPEG data for the poster

    public Event() {
        // Required empty constructor for Firestore deserialization
    }

    /**
     * Constructs an Event with minimal information.
     * <p>
     * Other attributes can be set later via setter methods.
     * </p>
     *
     * @param title       Title of the event
     * @param description Description of the event
     * @param capacity    Maximum participants allowed
     * @param organizer   Organizer of the event
     */
    public Event(String title, String description, int capacity, User organizer){
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.tag = "General";
        this.description = description;
        this.capacity = capacity;
        this.organizer = organizer;
        this.location = null;
        this.waitingList = new WaitingList();
        this.date = null;
        this.regStartDate = null;
        this.regEndDate = null;
        this.eventState = EventState.NOT_STARTED;
        this.waitingListCapacity = -1;
        this.geolocationRequired = false;
    }

    public byte[] getPosterBytes() {
        return posterBytes;
    }

    public void setPosterBytes(byte[] posterBytes) {
        this.posterBytes = posterBytes;
    }


    /** @return the title of the event */
    public String getTitle() {
        return title;
    }

    /** @param title Sets the event title */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return tag of event
     */
    public String getTag(){ return tag;}

    /**
     * @param tag Set the tag of the event
     */
    public void setTag(String tag){
        if (tag == null || tag.trim().isEmpty()){
            this.tag = "General";
        } else {
            tag = tag.substring(0,1).toUpperCase() + tag.substring(1).toLowerCase();
            this.tag = tag;
        }
    }

    /** @return the event description */
    public String getDescription() {
        return description;
    }

    /** @param description Sets the event description */
    public void setDescription(String description) {
        this.description = description;
    }

    /** @return the maximum number of participants */
    public int getCapacity() {
        return capacity;
    }

    /** @param capacity Sets the event capacity */
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    /** @return the event location */
    public String getLocation() {
        return location;
    }

    /** @param location Sets the event location */
    public void setLocation(String location) {
        this.location = location;
    }

    /** @return the date and time of the event */
    public Timestamp getDate() {
        return date;
    }

    /** @param date Sets the event date */
    public void setDate(Timestamp date) {
        this.date = date;
    }

    /** @return the registration start date */
    public Timestamp getRegStartDate() {
        return regStartDate;
    }

    /** @param regStartDate Sets the registration start date */
    public void setRegStartDate(Timestamp regStartDate) {
        this.regStartDate = regStartDate;
    }

    /** @return the registration end date */
    public Timestamp getRegEndDate() {
        return regEndDate;
    }

    /** @param regEndDate Sets the registration end date */
    public void setRegEndDate(Timestamp regEndDate) {
        this.regEndDate = regEndDate;
    }

    /** @return the unique event ID */
    public String getId() {
        return id;
    }

    /** @param id Sets the event ID */
    public void setId(String id) {
        this.id = id;
    }

    /** @return the WaitingList associated with the event */
    public WaitingList getWaitingList() {
        return waitingList;
    }

    /** @param waitingList Sets the event waiting list */
    public void setWaitingList(WaitingList waitingList) {
        this.waitingList = waitingList;
    }

    /** @return the event organizer */
    public User getOrganizer() {
        return organizer;
    }

    /** @param organizer Sets the event organizer */
    public void setOrganizer(User organizer) {
        this.organizer = organizer;
    }

    /** @return the waiting list capacity */
    public int getWaitingListCapacity() {
        return waitingListCapacity;
    }

    /** Sets waiting list capacity and updates the internal WaitingList object
     * @param waitingListCapacity Maximum number of waiting list participants
     */
    public void setWaitingListCapacity(int waitingListCapacity) {
        this.waitingListCapacity = waitingListCapacity;
        waitingList.setCapacity(waitingListCapacity);
    }

    /** @return true if geolocation is required */
    public boolean isGeolocationRequired() {
        return geolocationRequired;
    }

    /** @param geolocationRequired Sets whether geolocation is required */
    public void setGeolocationRequired(boolean geolocationRequired) {
        this.geolocationRequired = geolocationRequired;
    }

    public EventState getEventState() {
        switch (this.eventState) {
            case NOT_STARTED:
                this.updateRegistrationState();
                break;
            case OPEN_FOR_REG:
                this.updateRegistrationState();
                break;
            case CLOSED_FOR_REG:
                this.updateRegistrationState();
                break;
            default:
                // do nothing for fixed states like SELECTED_ENTRANTS, ENDED, CANCELLED.
                break;
        }

        return this.eventState;
    }

    public void setEventState(EventState eventState) {
        this.eventState = eventState;
    }


    /**
     * Determines if the event is currently active based on registration dates.
     * <p>
     * If both registration start and end dates are null, returns false.
     * Otherwise, returns true if current time falls within registration period.
     * </p>
     *
     * @return true if event is active, false otherwise
     */
    public boolean isOpenForReg() {
        return !this.isBeforeStartDate() && !this.isPastEndDate();
    }

    /**
     *
     * @return a string with formatted start & end time
     */
    public String getFormattedRegWindow() {
        if (regStartDate == null && regEndDate == null) {
            return "Not set";
        }

        SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy hh:mm aa", Locale.getDefault());

        String startStr = (regStartDate != null) ? fmt.format(regStartDate.toDate()) : "N/A";
        String endStr   = (regEndDate != null) ? fmt.format(regEndDate.toDate())   : "N/A";

        return startStr + "  –  " + endStr;
    }


    /**
     * Adds a user to the waiting list.
     *
     * @param entrant User to add to the waiting list
     */
    public void addToWaitingList(User entrant){
        waitingList.addEntrant(entrant);
    }

    /**
     * Removes a user from the waiting list.
     *
     * @param entrant User to remove from the waiting list
     */
    public void removeFromWaitingList(User entrant){
        waitingList.removeEntrant(entrant);
    }


    /**
     * Adds a user to the event's waiting list based on waiting list capacity (if there is a set capacity).
     * <p>
     * Checks if waiting list capacity is not exceeded and if the user
     * is not already in the list.
     * </p>
     *
     * @param entrant User joining the event
     */
    public void joinEvent(User entrant){
        if(waitingListCapacity != -1){
            if(waitingList.getList().size() < waitingListCapacity && !inWaitingList(entrant)){
                waitingList.addEntrant(entrant);
            }
        }else{
            waitingList.addEntrant(entrant);
        }
    }

    /**
     * Checks if a user is already in the waiting list.
     *
     * @param entrant User to check
     * @return true if user is in waiting list, false otherwise
     */
    public boolean inWaitingList(User entrant) {
        return this.waitingList.checkEntrant(entrant);
    }
    /** Convenience: same as inWaitingList but null-safe. */
    public boolean containsUser(User user) {
        if (user == null) {
            return false;
        }
        return waitingList.checkEntrant(user);
    }

    /**
     * Updates a waiting-list entrant's state (e.g., SELECTED, CANCELED).
     * @return true if updated successfully.
     */
    public boolean updateEntrantState(User entrant, WaitingListState state) {
        return this.waitingList.updateEntrantState(entrant, state);
    }

    private boolean isBeforeStartDate() {
        return (new Timestamp(new Date()).compareTo(this.regStartDate) < 0);
    }

    private boolean isPastEndDate() {
        return (new Timestamp(new Date()).compareTo(this.regEndDate) >= 0);
    }
    /**
     * Updates the eventState based on the current time relative to the
     * registration window:
     * - NOT_STARTED  → before registration start
     * - OPEN_FOR_REG → within the registration window
     * - CLOSED_FOR_REG → after registration end
     */
    public void updateRegistrationState() {
        if (this.isBeforeStartDate()) {
            this.eventState = EventState.NOT_STARTED;
        }
        else if (isOpenForReg()) {
            this.eventState = EventState.OPEN_FOR_REG;
        }
        else if (this.isPastEndDate()) {
            this.eventState = EventState.CLOSED_FOR_REG;
        }
    }

    public void runLottery() {
        if (waitingList == null || waitingList.getList() == null) {
            return;
        }

        // Count how many spots are already taken
        int alreadyIn = 0;
        for (Pair<User, WaitingListState> entry : waitingList.getList()) {
            WaitingListState s = entry.second;
            if (s == WaitingListState.SELECTED || s == WaitingListState.ACCEPTED) {
                alreadyIn++;
            }
        }

        int remainingSpots = capacity - alreadyIn;
        if (remainingSpots <= 0) {
            return;
        }

        // Unified lottery: draws from ENTERED + NOT_SELECTED
        LotterySystem.lotteryDraw(waitingList.getList(), remainingSpots);

        this.eventState = EventState.SELECTED_ENTRANTS;
    }

    @NonNull
    public WaitingListState getWaitingListStateForUser(@Nullable User user) {
        if (user == null) {
            return WaitingListState.NOT_IN;
        }
        return getWaitingListStateForUserId(user.getUserId());
    }

    @NonNull
    public WaitingListState getWaitingListStateForUserId(@Nullable String userId) {
        if (userId == null ||
                waitingList == null ||
                waitingList.getList() == null) {
            return WaitingListState.NOT_IN;
        }

        for (Pair<User, WaitingListState> entry : waitingList.getList()) {
            User u = entry.first;
            if (u != null && userId.equals(u.getUserId())) {
                return entry.second != null ? entry.second : WaitingListState.NOT_IN;
            }
        }

        return WaitingListState.NOT_IN;
    }



    public void confirmEntrants() {
        this.eventState = EventState.CONFIRMED_ENTRANTS;
    }

    public void endEvent() {
        this.eventState = EventState.ENDED;
    }

    public void cancelEvent() {
        this.eventState = EventState.CANCELLED;
    }

}
