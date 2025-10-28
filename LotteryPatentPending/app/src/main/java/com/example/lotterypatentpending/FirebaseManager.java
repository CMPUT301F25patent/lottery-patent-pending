package com.example.lotterypatentpending;

import java.util.Map;


import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.CollectionReference;

import java.util.HashMap;
import java.util.Map;
public class FirebaseManager {
    // --- Firebase Instances ---
    private final FirebaseFirestore db;



    private FirebaseManager() {
        db = FirebaseFirestore.getInstance();
    }


    // generic user methods, will be updated when user class is looked at

    public void addUser(String userId, Map<String, Object> userData) {
        db.collection("users").document(userId).set(userData)
                .addOnSuccessListener(aVoid -> {
                    System.out.println("User added successfully.");
                })
                .addOnFailureListener(e -> {
                    System.err.println("Error adding user: " + e.getMessage());
                });
    }

    public void getUser(String userId, FirebaseCallback<DocumentSnapshot> callback) {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(callback::onSuccess)
                .addOnFailureListener(callback::onFailure);
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
