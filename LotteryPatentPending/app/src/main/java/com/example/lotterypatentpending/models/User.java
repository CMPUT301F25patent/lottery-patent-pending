package com.example.lotterypatentpending.models;

import java.util.List;
import java.util.ArrayList;


public class User {

    // Firestore will bind the doc id into this field automatically

    private String userId;
    private String name;
    private String email;
    private String contactInfo;
    private boolean isAdmin;

    private List<String> joinedEventIds = new ArrayList<>();
    private List<String> acceptedEventIds = new ArrayList<>();
    private List<String> declinedEventIds = new ArrayList<>();
    private List<String> pastEventIds = new ArrayList<>();      // optional history
    private boolean notificationsOptIn = true;
    private List<Event> organizedEvents = new ArrayList<Event>();


    //For firebase, needs empty constructor
    public User(){}

    public User(String userId, String name, String email, String contactInfo, boolean isAdmin) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.contactInfo = contactInfo;
        this.isAdmin = isAdmin;
    }

    public User(String userId, String name, String email, String contactInfo) {
        this(userId, name, email, contactInfo, false);
    }
    public boolean isNotificationsOptIn() { return notificationsOptIn; }
    public void setNotificationsOptIn(boolean notificationsOptIn) { this.notificationsOptIn = notificationsOptIn; }

    public List<String> getJoinedEventIds() { return joinedEventIds; }
    public void setJoinedEventIds(List<String> joinedEventIds) { this.joinedEventIds = joinedEventIds; }

    public List<String> getAcceptedEventIds() { return acceptedEventIds; }
    public void setAcceptedEventIds(List<String> acceptedEventIds) { this.acceptedEventIds = acceptedEventIds; }

    public List<String> getDeclinedEventIds() { return declinedEventIds; }
    public void setDeclinedEventIds(List<String> declinedEventIds) { this.declinedEventIds = declinedEventIds; }

    public List<String> getPastEventIds() { return pastEventIds; }
    public void setPastEventIds(List<String> pastEventIds) { this.pastEventIds = pastEventIds; }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }


    // Joined events
    public void addJoinedEvent(String eventId) {
        if (!joinedEventIds.contains(eventId)) {
            joinedEventIds.add(eventId);
        }
    }

    public void removeJoinedEvent(String eventId) {
        if (joinedEventIds != null) {
            joinedEventIds.remove(eventId);
        }
    }

    // Accepted events
    public void addAcceptedEvent(String eventId) {
        if (!acceptedEventIds.contains(eventId)) {
            acceptedEventIds.add(eventId);
        }
    }

    public void removeAcceptedEvent(String eventId) {
        if (acceptedEventIds != null) {
            acceptedEventIds.remove(eventId);
        }
    }

    // Declined events
    public void addDeclinedEvent(String eventId) {
        if (!declinedEventIds.contains(eventId)) {
            declinedEventIds.add(eventId);
        }
    }

    public void removeDeclinedEvent(String eventId) {
        if (declinedEventIds != null) {
            declinedEventIds.remove(eventId);
        }
    }

    // Past events
    public void addPastEvent(String eventId) {
        if (!pastEventIds.contains(eventId)) {
            pastEventIds.add(eventId);
        }
    }

    public void removePastEvent(String eventId) {
        if (pastEventIds != null) {
            pastEventIds.remove(eventId);
        }
    }

    public List<Event> getOrganizedEvents(){
        return organizedEvents;
    }

    public void addOrganizedEvent(Event event){
        organizedEvents.add(event);
    }

    public Event createEvent(String title, String description, int capacity){
        Event newEvent = new Event(title, description, capacity, this);
        addOrganizedEvent(newEvent);
        return newEvent;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId='" + getUserId() + '\'' +
                ", name='" + getName() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", joinedEventIds=" + joinedEventIds +
                ", acceptedEventIds=" + acceptedEventIds +
                ", declinedEventIds=" + declinedEventIds +
                ", pastEventIds=" + pastEventIds +
                ", notificationsOptIn=" + notificationsOptIn +
                ", isAdmin=" + isAdmin +
                ", organizedEvents=" + organizedEvents +
                '}';
    }
}


