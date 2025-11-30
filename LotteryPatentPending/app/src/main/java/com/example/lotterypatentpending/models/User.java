package com.example.lotterypatentpending.models;
import com.google.firebase.firestore.Exclude;

import java.util.List;
import java.util.ArrayList;


/**
 * Represents a user in the application. This class holds user profile information,
 * their administrative status, and lists of events they have interacted with or organized.
 */
public class User {

    /** The unique ID of the user, typically bound automatically by Firestore. */
    private String userId;
    /** The display name of the user. */
    private String name;
    /** The email address of the user. */
    private String email;
    /** The primary contact information for the user. */
    private String contactInfo;
    /** Flag indicating if the user has administrator privileges. */
    private boolean isAdmin;
    /** The current geographical location of the user (can be null). */
    private UserLocation location = null;

    /** List of event IDs the user has signed up or joined. */
    private List<String> joinedEventIds = new ArrayList<>();
    /** List of event IDs where the user has been selected/accepted as an entrant. */
    private List<String> acceptedEventIds = new ArrayList<>();
    /** List of event IDs where the user has declined participation. */
    private List<String> declinedEventIds = new ArrayList<>();
    /** Optional history of events the user has participated in that have concluded. */
    private List<String> pastEventIds = new ArrayList<>();      // optional history
    /** Flag indicating if the user is opted-in to receive notifications. */
    private boolean notificationsOptIn = true;


    //Use Organized Events View Model for livedata.
    //    private List<Event> organizedEvents = new ArrayList<Event>();


    /**
     * No-argument constructor required for Firestore object deserialization.
     */
    public User(){}

    /**
     * Constructs a new User with all core fields.
     * @param userId The unique ID of the user.
     * @param name The name of the user.
     * @param email The email address of the user.
     * @param contactInfo The contact information for the user.
     * @param isAdmin A flag indicating if the user is an administrator.
     */
    public User(String userId, String name, String email, String contactInfo, boolean isAdmin) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.contactInfo = contactInfo;
        this.isAdmin = isAdmin;
    }

    /**
     * Constructs a new non-admin User.
     * @param userId The unique ID of the user.
     * @param name The name of the user.
     * @param email The email address of the user.
     * @param contactInfo The contact information for the user.
     */
    public User(String userId, String name, String email, String contactInfo) {
        this(userId, name, email, contactInfo, false);
    }

    /** Gets the notification opt-in status. */
    public boolean isNotificationsOptIn() { return notificationsOptIn; }

    /** Sets the notification opt-in status. */
    public void setNotificationsOptIn(boolean notificationsOptIn) { this.notificationsOptIn = notificationsOptIn; }

    /** Gets the list of joined event IDs. */
    public List<String> getJoinedEventIds() { return joinedEventIds; }

    /** Sets the list of joined event IDs. */
    public void setJoinedEventIds(List<String> joinedEventIds) { this.joinedEventIds = joinedEventIds; }

    /** Gets the list of accepted event IDs. */
    public List<String> getAcceptedEventIds() { return acceptedEventIds; }

    /** Sets the list of accepted event IDs. */
    public void setAcceptedEventIds(List<String> acceptedEventIds) { this.acceptedEventIds = acceptedEventIds; }

    /** Gets the list of declined event IDs. */
    public List<String> getDeclinedEventIds() { return declinedEventIds; }

    /** Sets the list of declined event IDs. */
    public void setDeclinedEventIds(List<String> declinedEventIds) { this.declinedEventIds = declinedEventIds; }

    /** Gets the list of past event IDs. */
    public List<String> getPastEventIds() { return pastEventIds; }

    /** Sets the list of past event IDs. */
    public void setPastEventIds(List<String> pastEventIds) { this.pastEventIds = pastEventIds; }

    /** Gets the unique user ID. */
    public String getUserId() {
        return userId;
    }

    /** Sets the unique user ID. */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /** Gets the user's name. */
    public String getName() {
        return name;
    }

    /** Sets the user's name. */
    public void setName(String name) {
        this.name = name;
    }

    /** Gets the user's email address. */
    public String getEmail() {
        return email;
    }

    /** Sets the user's email address. */
    public void setEmail(String email) {
        this.email = email;
    }

    /** Gets the user's contact information. */
    public String getContactInfo() {
        return contactInfo;
    }

    /** Sets the user's contact information. */
    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }

    /** Checks if the user has administrator privileges. */
    public boolean isAdmin() {
        return isAdmin;
    }

    /** Sets the user's administrator status. */
    public void setAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }


    /**
     * Adds an event ID to the list of joined events, preventing duplicates.
     * @param eventId The ID of the event to add.
     */
    public void addJoinedEvent(String eventId) {
        if (!joinedEventIds.contains(eventId)) {
            joinedEventIds.add(eventId);
        }
    }

    /**
     * Removes an event ID from the list of joined events.
     * @param eventId The ID of the event to remove.
     */
    public void removeJoinedEvent(String eventId) {
        if (joinedEventIds != null) {
            joinedEventIds.remove(eventId);
        }
    }

    // Accepted events
    /**
     * Adds an event ID to the list of accepted events, preventing duplicates.
     * @param eventId The ID of the event to add.
     */
    public void addAcceptedEvent(String eventId) {
        if (!acceptedEventIds.contains(eventId)) {
            acceptedEventIds.add(eventId);
        }
    }

    /**
     * Removes an event ID from the list of accepted events.
     * @param eventId The ID of the event to remove.
     */
    public void removeAcceptedEvent(String eventId) {
        if (acceptedEventIds != null) {
            acceptedEventIds.remove(eventId);
        }
    }

    // Declined events
    /**
     * Adds an event ID to the list of declined events, preventing duplicates.
     * @param eventId The ID of the event to add.
     */
    public void addDeclinedEvent(String eventId) {
        if (!declinedEventIds.contains(eventId)) {
            declinedEventIds.add(eventId);
        }
    }

    /**
     * Removes an event ID from the list of declined events.
     * @param eventId The ID of the event to remove.
     */
    public void removeDeclinedEvent(String eventId) {
        if (declinedEventIds != null) {
            declinedEventIds.remove(eventId);
        }
    }

    // Past events
    /**
     * Adds an event ID to the list of past events, preventing duplicates.
     * @param eventId The ID of the event to add.
     */
    public void addPastEvent(String eventId) {
        if (!pastEventIds.contains(eventId)) {
            pastEventIds.add(eventId);
        }
    }

    /**
     * Removes an event ID from the list of past events.
     * @param eventId The ID of the event to remove.
     */
    public void removePastEvent(String eventId) {
        if (pastEventIds != null) {
            pastEventIds.remove(eventId);
        }
    }

    /** Gets the user's current location. */
    public UserLocation getLocation() {
        return location;
    }

    /** Sets the user's current location object. */
    public void setLocation(UserLocation location) {
        this.location = location;
    }

    /** Creates a new {@link UserLocation} object and sets it as the user's location. */
    public void addLocation(double lat, double lng){
        this.location = new UserLocation(lat, lng);
    }

    /**
     * Returns a string representation of the User object, including key fields.
     * @return A string summary of the user.
     */
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
                '}';
    }

    /**
     * Compares two User objects based on their userId.
     * @param o The object to compare against.
     * @return True if the userIds are equal, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        User other = (User)o;
        if (this.userId == null && other.userId == null) {
            return true;
        }
        if (this.userId == null || other.userId == null) {
            return false;
        }
        return this.userId.equals(other.userId);
    }

    /**
     * Returns a hash code value for the object, based on the userId.
     * @return The hash code of the userId, or 0 if the userId is null.
     */
    @Override
    public int hashCode() {
        return userId != null ? userId.hashCode() : 0;
    }
}