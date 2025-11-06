package com.example.lotterypatentpending.models;

import android.util.Log;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import com.example.lotterypatentpending.models.Event;
import com.example.lotterypatentpending.models.Notification;
import com.example.lotterypatentpending.models.User;
import com.example.lotterypatentpending.models.WaitingListState;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.firestore.DocumentSnapshot;
import com.example.lotterypatentpending.models.FirebaseManager.FirebaseCallback;

public class FirebaseManager {
    // --- Firebase Instances ---
    private static FirebaseManager instance;
    private final FirebaseFirestore db;

    private FirebaseManager() {
        db = FirebaseFirestore.getInstance();
    }

    public static FirebaseManager getInstance() {
        if (instance == null) {
            instance = new FirebaseManager();
        }
        return instance;
    }

    // generic user methods, will be updated when user class is looked at

    //utilizes user  class, add update, delete and get methods added
    public void addOrUpdateUser(User user) {
        db.collection("users").document(user.getUserId()).set(user)
                .addOnSuccessListener(aVoid -> System.out.println("User saved successfully: " + user.getUserId()))
                .addOnFailureListener(e -> System.err.println("Error saving user: " + e.getMessage()));
    }

    public void getUser(String userId, FirebaseCallback<DocumentSnapshot> callback) {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(callback::onSuccess)
                .addOnFailureListener(callback::onFailure);
    }
    public void getAllUsers(FirebaseCallback<QuerySnapshot> callback) {
        db.collection("users").get()
                .addOnSuccessListener(callback::onSuccess)
                .addOnFailureListener(callback::onFailure);
    }

    public void deleteUser(String userId) {
        db.collection("users").document(userId).delete()
                .addOnSuccessListener(aVoid -> System.out.println("User deleted successfully: " + userId))
                .addOnFailureListener(e -> System.err.println("Error deleting user: " + e.getMessage()));
    }


    // mapping event objects to firestore
    private Map<String, Object> eventToMap(Event event) {
        Map<String, Object> data = new HashMap<>();

        data.put("id", event.getId());
        data.put("title", event.getTitle());
        data.put("description", event.getDescription());
        data.put("capacity", event.getCapacity());
        data.put("location", event.getLocation());

        // Dates & times as strings
        data.put("date", event.getDate() != null ? event.getDate().toString() : null);
        data.put("time", event.getTime() != null ? event.getTime().toString() : null);
        data.put("regStartDate", event.getRegStartDate() != null ? event.getRegStartDate().toString() : null);
        data.put("regStartTime", event.getRegStartTime() != null ? event.getRegStartTime().toString() : null);
        data.put("regEndDate", event.getRegEndDate() != null ? event.getRegEndDate().toString() : null);
        data.put("regEndTime", event.getRegEndTime() != null ? event.getRegEndTime().toString() : null);

        // Organizer is just a User
        if (event.getOrganizer() != null) {
            Map<String, Object> organizerMap = new HashMap<>();
            organizerMap.put("userId", event.getOrganizer().getUserId());
            organizerMap.put("name", event.getOrganizer().getName());
            organizerMap.put("email", event.getOrganizer().getEmail());
            organizerMap.put("contactInfo", event.getOrganizer().getContactInfo());
            organizerMap.put("isAdmin", event.getOrganizer().isAdmin());
            data.put("organizer", organizerMap);
        } else {
            data.put("organizer", null);
        }

        return data;
    }


    //Converts Firestore data back into an Event object
    private Event mapToEvent(Map<String, Object> data) {
        if (data == null) return null;

        String title = (String) data.get("title");
        String description = (String) data.get("description");
        String location = (String) data.get("location");

        int capacity = 0;
        Object capObj = data.get("capacity");
        if (capObj instanceof Long) capacity = ((Long) capObj).intValue();
        else if (capObj instanceof Integer) capacity = (Integer) capObj;

        // Organizer is a User
        User organizer = null;
        Map<String, Object> organizerMap = (Map<String, Object>) data.get("organizer");
        if (organizerMap != null) {
            String id = (String) organizerMap.get("userId");
            String name = (String) organizerMap.get("name");
            String email = (String) organizerMap.get("email");
            String contact = (String) organizerMap.get("contactInfo");
            boolean isAdmin = organizerMap.get("isAdmin") != null && (boolean) organizerMap.get("isAdmin");

            organizer = new User(id, name, email, contact, isAdmin);
        }

        Event event = new Event(title, description, capacity, organizer);
        event.setLocation(location);



        if (data.get("id") != null)
            event.setId((String) data.get("id"));

        return event;
    }


    public void addOrUpdateEvent(String eventId, Event event) {
        if (eventId == null || eventId.isEmpty()) {
            eventId = event.getId(); // fallback to event’s own ID
        }

        Map<String, Object> eventData = eventToMap(event);
        db.collection("events").document(eventId).set(eventData)
                .addOnSuccessListener(aVoid ->
                        Log.d("FirebaseManager", "Event saved successfully: " + event.getTitle()))
                .addOnFailureListener(e ->
                        Log.e("FirebaseManager", "Error saving event: " + e.getMessage()));
    }

    public void getEvent(String eventId, FirebaseCallback<Event> callback) {
        db.collection("events").document(eventId).get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        try {
                            Event event = mapToEvent(snapshot.getData());
                            callback.onSuccess(event);
                        } catch (Exception e) {
                            callback.onFailure(e);
                        }
                    } else {
                        callback.onFailure(new FirebaseFirestoreException(
                                "Event not found", FirebaseFirestoreException.Code.NOT_FOUND));
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    public void getAllEvents(FirebaseCallback<QuerySnapshot> callback) {
        db.collection("events").get()
                .addOnSuccessListener(callback::onSuccess)
                .addOnFailureListener(callback::onFailure);
    }

    public void deleteEvent(String eventId) {
        db.collection("events").document(eventId).delete()
                .addOnSuccessListener(aVoid ->
                        System.out.println("Event deleted successfully: " + eventId))
                .addOnFailureListener(e ->
                        System.err.println("Error deleting event: " + e.getMessage()));
    }

    public void addEntrantToWaitingList(User entrant, WaitingListState state, String eventId) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", entrant.getUserId());
        data.put("name", entrant.getName());
        data.put("state", state.toString());  // store enum as String

        db.collection("events")
                .document(eventId)
                .collection("waitingList")
                .document(entrant.getUserId())
                .set(data)
                .addOnSuccessListener(aVoid ->
                        System.out.println("Entrant added to waiting list: " + entrant.getName()))
                .addOnFailureListener(e ->
                        System.err.println("Error adding entrant: " + e.getMessage()));
    }

    // Updates an entrant’s waiting list state (e.g., SELECTED, ACCEPTED, DECLINED).
    public void updateEntrantState(String eventId, String entrantId, WaitingListState newState) {
        db.collection("events")
                .document(eventId)
                .collection("waitingList")
                .document(entrantId)
                .update("state", newState.name())
                .addOnSuccessListener(aVoid ->
                        System.out.println("Entrant " + entrantId + " state updated to " + newState))
                .addOnFailureListener(e ->
                        System.err.println("Error updating state: " + e.getMessage()));
    }

    //Retrieves all entrants in a given event’s waiting list.
    public void getWaitingList(String eventId, FirebaseCallback<QuerySnapshot> callback) {
        db.collection("events")
                .document(eventId)
                .collection("waitingList")
                .get()
                .addOnSuccessListener(callback::onSuccess)
                .addOnFailureListener(callback::onFailure);
    }

    //Removes an entrant from the waiting list.

    public void removeEntrantFromWaitingList(String eventId, String entrantId) {
        db.collection("events")
                .document(eventId)
                .collection("waitingList")
                .document(entrantId)
                .delete()
                .addOnSuccessListener(aVoid ->
                        System.out.println("Entrant removed from waiting list: " + entrantId))
                .addOnFailureListener(e ->
                        System.err.println("Error removing entrant: " + e.getMessage()));
    }

    public void addJoinedEventToEntrant(Event event, String userId) {
        db.collection("users")
                .document(userId)
                .collection("joinedEventIds")
                .document(event.getId())
                .set(event.getId())
                .addOnSuccessListener(aVoid -> {
                    System.out.println("Event added to entrant's joined list");
                })
                .addOnFailureListener(e -> {
                    System.err.println("Error adding event to entrant's joined list: " + e.getMessage());
                });
    }

    // fetches events by event ID, utilized for deleting organizers by event
    public void getEventById(String eventId, FirebaseCallback<DocumentSnapshot> callback) {
        db.collection("events")
                .document(eventId)
                .get()
                .addOnSuccessListener(callback::onSuccess)
                .addOnFailureListener(callback::onFailure);
    }



    // for lsteners
    public interface FirebaseCallback<T> {
        void onSuccess(T result);
        void onFailure(Exception e);
    }
}

