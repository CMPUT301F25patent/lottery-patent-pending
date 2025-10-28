package com.example.lotterypatentpending;
import java.util.List;
import java.util.ArrayList;


public class Entrant extends User {
    private List<String> joinedEventIds = new ArrayList<>();
    private List<String> acceptedEventIds = new ArrayList<>();
    private List<String> declinedEventIds = new ArrayList<>();
    private List<String> pastEventIds = new ArrayList<>();      // optional history
    private boolean notificationsOptIn = true;

    public Entrant(String userId, String name, String email, String contactInfo) {
        super(userId, name, email, contactInfo, "Entrant");
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

    @Override
    public String toString() {
        return "Entrant{" +
                "userId='" + getUserId() + '\'' +
                ", name='" + getName() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", joinedEventIds=" + joinedEventIds +
                ", acceptedEventIds=" + acceptedEventIds +
                ", declinedEventIds=" + declinedEventIds +
                ", pastEventIds=" + pastEventIds +
                ", notificationsOptIn=" + notificationsOptIn +
                '}';
    }
}


