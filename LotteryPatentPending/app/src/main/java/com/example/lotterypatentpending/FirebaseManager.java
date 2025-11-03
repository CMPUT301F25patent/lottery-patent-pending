package com.example.lotterypatentpending;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import java.util.HashMap;

import com.example.lotterypatentpending.models.WaitingListState;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.auth.User;
import com.google.firebase.storage.FirebaseStorage;

public class FirebaseManager {
    // --- Firebase Instances ---
    private final FirebaseFirestore db;


    private static FirebaseManager instance;
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
        data.put("title", event.getTitle());
        data.put("description", event.getDescription());
        data.put("capacity", event.getCapacity());
        data.put("location", event.getLocation());

        // Convert LocalDate and LocalTime to Strings for Firebase
        data.put("date", event.getDate() != null ? event.getDate().toString() : null);
        data.put("time", event.getTime() != null ? event.getTime().toString() : null);
        data.put("regStartDate", event.getRegStartDate() != null ? event.getRegStartDate().toString() : null);
        data.put("regStartTime", event.getRegStartTime() != null ? event.getRegStartTime().toString() : null);
        data.put("regEndDate", event.getRegEndDate() != null ? event.getRegEndDate().toString() : null);
        data.put("regEndTime", event.getRegEndTime() != null ? event.getRegEndTime().toString() : null);

        return data;
    }

    //Converts Firestore data back into an Event object
    private Event mapToEvent(Map<String, Object> data) {
        String title = (String) data.get("title");
        String description = (String) data.get("description");
        String location = (String) data.get("location");
        int capacity = ((Long) data.get("capacity")).intValue();

        Event event = new Event(title, description, capacity);
        event.setLocation(location);

        // Convert Strings back to LocalDate and LocalTime
        if (data.get("date") != null) event.setDate(LocalDate.parse((String) data.get("date")));
        if (data.get("time") != null) event.setTime(LocalTime.parse((String) data.get("time")));
        if (data.get("regStartDate") != null) event.setRegStartDate(LocalDate.parse((String) data.get("regStartDate")));
        if (data.get("regStartTime") != null) event.setRegStartTime(LocalTime.parse((String) data.get("regStartTime")));
        if (data.get("regEndDate") != null) event.setRegEndDate(LocalDate.parse((String) data.get("regEndDate")));
        if (data.get("regEndTime") != null) event.setRegEndTime(LocalTime.parse((String) data.get("regEndTime")));

        return event;
    }


    //

    public void addOrUpdateEvent(String eventId, Event event) {
        Map<String, Object> eventData = eventToMap(event);
        db.collection("events").document(eventId).set(eventData)
                .addOnSuccessListener(aVoid ->
                        System.out.println("Event saved successfully: " + event.getTitle()))
                .addOnFailureListener(e ->
                        System.err.println("Error saving event: " + e.getMessage()));
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
    //
    public void addEntrantToWaitingList(String eventId, WaitingListEntry entry) {
        Map<String, Object> data = new HashMap<>();
        data.put("entrantId", entry.getEntrantId());
        data.put("entrantName", entry.getEntrantName());
        data.put("state", entry.getState().name());  // store enum as String

        db.collection("events")
                .document(eventId)
                .collection("waitingList")
                .document(entry.getEntrantId())
                .set(data)
                .addOnSuccessListener(aVoid ->
                        System.out.println("Entrant added to waiting list: " + entry.getEntrantName()))
                .addOnFailureListener(e ->
                        System.err.println("Error adding entrant: " + e.getMessage()));
    }

    //Updates an entrant’s waiting list state (e.g., SELECTED, ACCEPTED, DECLINED).

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


    // generic notification add, will updated after looking at notification class
    public void logNotification(String notificationId, Map<String, Object> notificationData) {
        db.collection("notifications").document(notificationId).set(notificationData)
                .addOnSuccessListener(aVoid -> System.out.println("Notification logged."))
                .addOnFailureListener(e -> System.err.println("Error logging notification: " + e.getMessage()));
    }

    // for lsteners
    public interface FirebaseCallback<T> {
        void onSuccess(T result);
        void onFailure(Exception e);
    }
}
