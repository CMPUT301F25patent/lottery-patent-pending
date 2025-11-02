package com.example.lotterypatentpending;

import java.util.Map;
import java.util.HashMap;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.auth.User;
import com.google.firebase.storage.FirebaseStorage;

public class FirebaseManager {
    // --- Firebase Instances ---
    private final FirebaseFirestore db;



    private FirebaseManager() {
        db = FirebaseFirestore.getInstance();
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
    // generic event add, will update after looking at event class

    public void addEvent(String eventId, Map<String, Object> eventData) {
        db.collection("events").document(eventId).set(eventData)
                .addOnSuccessListener(aVoid -> {
                    System.out.println("Event created successfully.");
                })
                .addOnFailureListener(e -> {
                    System.err.println("Error creating event: " + e.getMessage());
                });
    }



    public void getEvent(String eventId, FirebaseCallback<DocumentSnapshot> callback) {
        db.collection("events").document(eventId).get()
                .addOnSuccessListener(callback::onSuccess)
                .addOnFailureListener(callback::onFailure);
    }

    public void getAllEvents(FirebaseCallback<QuerySnapshot> callback) {
        db.collection("events").get()
                .addOnSuccessListener(callback::onSuccess)
                .addOnFailureListener(callback::onFailure);
    }

    // generic waitinglist add, will updated after looking at waitinglistclass

    public void addToWaitingList(String eventId, String entrantId, Map<String, Object> entrantData) {
        CollectionReference waitingList = db.collection("events").document(eventId).collection("waitingList");
        waitingList.document(entrantId).set(entrantData)
                .addOnSuccessListener(aVoid -> {
                    System.out.println("Entrant added to waiting list.");
                })
                .addOnFailureListener(e -> {
                    System.err.println("Error adding entrant: " + e.getMessage());
                });
    }

    public void removeFromWaitingList(String eventId, String entrantId) {
        db.collection("events").document(eventId)
                .collection("waitingList").document(entrantId).delete()
                .addOnSuccessListener(aVoid -> System.out.println("Entrant removed from waiting list."))
                .addOnFailureListener(e -> System.err.println("Error removing entrant: " + e.getMessage()));
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
