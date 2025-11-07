package com.example.lotterypatentpending.models;

import android.os.Build;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.example.lotterypatentpending.exceptions.UserNotFoundException;
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

    public void getUser(String userId, FirebaseCallback<User> callback) {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            user.setUserId(documentSnapshot.getId());
                            callback.onSuccess(user);
                        }
                        else {
                            callback.onFailure(new UserNotFoundException("User not found."));
                        }
                    }
                    else {
                        callback.onFailure(new UserNotFoundException("User not found."));
                    }
                })
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

    public void addEventToDB(Event event){
        Map <String, Object> eventMap = eventToMap(event);
        db.collection("events").document(event.getId()).set(eventMap)
                .addOnSuccessListener(aVoid -> {
                    Log.d("FIREBASE", "Event saved successfully");
                })
                .addOnFailureListener(e -> {
                    Log.e("FIREBASE", "Failed to save event", e);
                });
    };

    public void deleteEventFromDB(Event event){
        CollectionReference eventsRef = db.collection("Events");
        DocumentReference eventDocRef = eventsRef.document(event.getId());
        eventDocRef.delete();
    };

//    public void updateEventInDB(Event event){
//        CollectionReference eventsRef = db.collection("Events");
//        DocumentReference eventDocRef = eventsRef.document(event.getId());
//        eventDocRef.delete();
//    };
    private Map<String, Object> userToMap(User user) {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", user.getUserId());
        map.put("name", user.getName());
        map.put("email", user.getEmail());
        map.put("contactInfo", user.getContactInfo());
        map.put("isAdmin", user.isAdmin());
        return map;
    }

    // mapping event objects to firestore
    private Map<String, Object> eventToMap(Event event) {
        Map<String, Object> data = new HashMap<>();

        data.put("id", event.getId());
        data.put("title", event.getTitle());
        data.put("description", event.getDescription());
        data.put("capacity", event.getCapacity());
        data.put("location", event.getLocation());
        data.put("qrCode", event.getQrCode() != null
                ? event.getQrCode().toPayload()
                : null);

        data.put("waitingListCapacity", event.getWaitingListCapacity());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a");

            // Dates & times as strings
            data.put("event_date", event.getDate() != null
                    ? event.getDate().format(formatter)
                : null);

            data.put("regStartDate", event.getRegStartDate() != null
                    ? event.getRegStartDate().format(formatter)
                    : null);

            data.put("regEndDate", event.getRegEndDate() != null
                    ? event.getRegEndDate().format(formatter)
                    : null);
        }


        // Organizer is just a User
//        if (event.getOrganizer() != null) {
//            Map<String, Object> organizerMap = new HashMap<>();
//            organizerMap.put("userId", event.getOrganizer().getUserId());
//            organizerMap.put("name", event.getOrganizer().getName());
//            organizerMap.put("email", event.getOrganizer().getEmail());
//            organizerMap.put("contactInfo", event.getOrganizer().getContactInfo());
//            organizerMap.put("isAdmin", event.getOrganizer().isAdmin());
//            data.put("organizer", organizerMap);
//        } else {
//            data.put("organizer", null);
//        }
        // Organizer is just a User
        if (event.getOrganizer() != null) {
            data.put("organizer", event.getOrganizer().getUserId());
        } else {
            data.put("organizer", null);
        }

        // Entrants
        List<String> entrantsList = new ArrayList<>();
        for (User u : event.getEntrants()) {
            entrantsList.add(u.getUserId());
        }
        data.put("entrants", entrantsList);

        data.put("waitingList", serializeWaitingList(event.getWaitingList().getList()));

        return data;

    }


    //Converts Firestore data back into an Event object
    public Event mapToEvent(Map<String, Object> data) {
        if (data == null) return null;

        // Basic string fields
        String title = (String) data.get("title");
        String description = (String) data.get("description");
        String location = (String) data.get("location");

        // Capacity handling
        int capacity = 0;
        Object capObj = data.get("capacity");
        if (capObj instanceof Long) capacity = ((Long) capObj).intValue();
        else if (capObj instanceof Integer) capacity = (Integer) capObj;

        // Organizer handling (can be Map or String)
        User organizer = null;
        Object orgObj = data.get("organizer");

        if (orgObj instanceof Map) {
            Map<String, Object> organizerMap = (Map<String, Object>) orgObj;
            String id = (String) organizerMap.get("userId");
            String name = (String) organizerMap.get("name");
            String email = (String) organizerMap.get("email");
            String contact = (String) organizerMap.get("contactInfo");
            boolean isAdmin = organizerMap.get("isAdmin") != null && (boolean) organizerMap.get("isAdmin");
            organizer = new User(id, name, email, contact, isAdmin);
        } else if (orgObj instanceof String) {
            organizer = new User();
            organizer.setName((String) orgObj);
        }

        // Create Event object
        Event event = new Event(title, description, capacity, organizer);
        event.setLocation(location);

        if (data.get("id") != null)
            event.setId((String) data.get("id"));

        // Date parsing - use SimpleDateFormat for compatibility
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());

        try {
            Object dateObj = data.get("date_time");
            if (dateObj != null) {
                Date parsed = sdf.parse(dateObj.toString());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && parsed != null) {
                    event.setDate(parsed.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
                }
            }
        } catch (Exception e) {
            Log.w("FirebaseManager", "Failed to parse date_time: " + e.getMessage());
        }

        try {
            Object regStartObj = data.get("regStartDate");
            if (regStartObj != null) {
                Date parsed = sdf.parse(regStartObj.toString());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && parsed != null) {
                    event.setRegStartDate(parsed.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
                }
            }
        } catch (Exception e) {
            Log.w("FirebaseManager", "Failed to parse regStartDate: " + e.getMessage());
        }

        try {
            Object regEndObj = data.get("regEndDate");
            if (regEndObj != null) {
                Date parsed = sdf.parse(regEndObj.toString());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && parsed != null) {
                    event.setRegEndDate(parsed.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
                }
            }
        } catch (Exception e) {
            Log.w("FirebaseManager", "Failed to parse regEndDate: " + e.getMessage());
        }

        return event;
    }

    public List<Map<String, Object>> serializeWaitingList(List<Pair<User, WaitingListState>> list) {
        List<Map<String, Object>> out = new ArrayList<>();

        for (Pair<User, WaitingListState> entry : list) {
            Map<String, Object> map = new HashMap<>();
            map.put("uid", entry.first.getUserId());
            map.put("state", entry.second.name()); // store enum as string
            out.add(map);
        }

        return out;
    }

//    public List<Pair<User, WaitingListState>> deserializeWaitingList(
//            List<Map<String, Object>> storedList,
//            UserEventRepository userRepo // however you load users
//    ) {
//        List<Pair<User, WaitingListState>> out = new ArrayList<>();
//
//        for (Map<String, Object> map : storedList) {
//            String uid = (String) map.get("uid");
//            String stateName = (String) map.get("state");
//
//            User user = userRepo.getInstance().getUser(); // however you fetch users
//            WaitingListState state = WaitingListState.valueOf(stateName);
//
//            out.add(new Pair<>(user, state));
//        }
//
//        return out;
//    }



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

    public void getAllEvents(FirebaseCallback<ArrayList<Event>> callback) {
        db.collection("events").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayList<Event> events = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Map<String, Object> data = doc.getData();
                        if (data == null) continue;

                        Event event = mapToEvent(data);
                        if (event != null) {
                            event.setId(doc.getId());
                            events.add(event);
                        }
                    }
                    callback.onSuccess(events);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseManager", "Error getting all events: " + e.getMessage());
                    callback.onFailure(e);
                });
    }


    public void deleteEvent(String eventId) {
        db.collection("events")
                .document(eventId)
                .delete()
                .addOnSuccessListener(aVoid ->
                        Log.i("FirebaseManager", "Event deleted successfully: " + eventId))
                .addOnFailureListener(e ->
                        Log.e("FirebaseManager", "Error deleting event: " + e.getMessage()));
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

    public void removeJoinedEventFromEntrant(String eventId, String entrantId) {

    }


    // generic notification add, will updated after looking at notification class
    public void logNotification(Notification notification) {
        // If the notification already has an ID, reuse it; otherwise Firestore generates one.
        DocumentReference docRef;
        if (notification.getId() != null && !notification.getId().isEmpty()) {
            docRef = db.collection("notifications").document(notification.getId());
        } else {
            docRef = db.collection("notifications").document();
            notification.setId(docRef.getId());
        }

        // Ensure timestamps are filled if not already
        if (notification.getCreatedAt() == null) {
            notification.setCreatedAt(Timestamp.now().toDate());
        }

        docRef.set(notification)
                .addOnSuccessListener(aVoid ->
                        Log.d("FirebaseManager", "Notification logged: " + notification.getId()))
                .addOnFailureListener(e ->
                        Log.e("FirebaseManager", "Error logging notification: " + e.getMessage()));
    }

    public void getAllNotifications(FirebaseCallback<List<Notification>> callback) {
        db.collection("notifications")
                .orderBy("createdAt") // optional if stored as a Timestamp
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Notification> notifications = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot) {
                        Notification notif = doc.toObject(Notification.class);
                        notifications.add(notif);
                    }
                    callback.onSuccess(notifications);
                })
                .addOnFailureListener(callback::onFailure);
    }

    // fetches events by event ID, utilized for deleting organizers by event
    public void getEventById(String eventId, FirebaseCallback<DocumentSnapshot> callback) {
        db.collection("events")
                .document(eventId)
                .get()
                .addOnSuccessListener(callback::onSuccess)
                .addOnFailureListener(callback::onFailure);
    }



    // for listeners
    public interface FirebaseCallback<T> {
        void onSuccess(T result);
        void onFailure(Exception e);
    }
}

