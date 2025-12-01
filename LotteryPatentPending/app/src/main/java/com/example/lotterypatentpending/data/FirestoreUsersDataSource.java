package com.example.lotterypatentpending.data;

import android.util.Log;
import com.example.lotterypatentpending.models.WaitingListState;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class FirestoreUsersDataSource implements UserDataSource {

    private final FirebaseFirestore db;
    private static final String TAG = "UsersDataSource";

    public FirestoreUsersDataSource() {
        this.db = FirebaseFirestore.getInstance();
    }

    @Override
    public CompletableFuture<List<String>> getEntrantsByState(String eventId, WaitingListState state) {
        CompletableFuture<List<String>> future = new CompletableFuture<>();

        Log.d(TAG, "Fetching entrants for Event: " + eventId + " | Target State: " + state.name());

        db.collection("events").document(eventId).get()
                .addOnSuccessListener(snapshot -> {
                    List<String> matchingIds = new ArrayList<>();

                    if (snapshot.exists()) {
                        // 1. Get the raw data field
                        Object rawData = snapshot.get("waitingList");

                        if (rawData == null) {
                            Log.w(TAG, "waitingList field is NULL.");
                        }
                        // SCENARIO A: Data is stored as a Map (User ID is the key)
                        // This matches your logs: "Scanning object keys: [3hEQU...]"
                        else if (rawData instanceof Map) {
                            Map<?, ?> map = (Map<?, ?>) rawData;

                            // Check if this is a wrapper with a "list" key
                            if (map.containsKey("list") && map.get("list") instanceof List) {
                                processList((List<?>) map.get("list"), state, matchingIds);
                            }
                            // Otherwise, iterate the keys (User IDs)
                            else {
                                Log.d(TAG, "Processing WaitingList as Map (Key=UserID). Size: " + map.size());
                                for (Map.Entry<?, ?> entry : map.entrySet()) {
                                    Object key = entry.getKey();   // Likely the User ID
                                    Object val = entry.getValue(); // The User Object or Pair

                                    // If the key looks like an ID, and the value matches state, add the key
                                    if (isStateMatch(val, state)) {
                                        matchingIds.add(key.toString());
                                    }
                                    // Fallback: Check if the ID is inside the value object
                                    else if (val instanceof Map && isStateMatch(val, state)) {
                                        String innerId = extractUserId((Map<?,?>) val);
                                        if (innerId != null) matchingIds.add(innerId);
                                    }
                                }
                            }
                        }
                        // SCENARIO B: Data is stored as a List (The structure WaitingList.java expects)
                        else if (rawData instanceof List) {
                            Log.d(TAG, "Processing WaitingList as List (Standard).");
                            processList((List<?>) rawData, state, matchingIds);
                        }
                    } else {
                        Log.e(TAG, "Event document does not exist.");
                    }

                    Log.d(TAG, "Found " + matchingIds.size() + " IDs for state " + state.name());
                    future.complete(matchingIds);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Firestore error", e);
                    future.completeExceptionally(e);
                });

        return future;
    }

    /**
     * Logic to process a List of Pairs (matching WaitingList.java structure)
     */
    private void processList(List<?> list, WaitingListState targetState, List<String> matchingIds) {
        for (Object item : list) {
            if (item instanceof Map) {
                Map<?, ?> pairMap = (Map<?, ?>) item;

                // WaitingList.java uses Pair<User, State>
                // In Firestore: "first" = User, "second" = State
                Object stateObj = pairMap.get("second");
                Object userObj = pairMap.get("first");

                // Fallback for flat maps
                if (stateObj == null) stateObj = pairMap.get("state");
                if (userObj == null) userObj = pairMap;

                if (stateObj != null && stateObj.toString().equalsIgnoreCase(targetState.name())) {
                    if (userObj instanceof Map) {
                        String uid = extractUserId((Map<?, ?>) userObj);
                        if (uid != null) matchingIds.add(uid);
                    }
                }
            }
        }
    }

    /**
     * Helper to find the User ID regardless of field name
     */
    private String extractUserId(Map<?, ?> userMap) {
        String uid = (String) userMap.get("userId");
        if (uid == null) uid = (String) userMap.get("uid");
        if (uid == null) uid = (String) userMap.get("id");
        return uid;
    }

    /**
     * Checks if a value object matches the target state
     */
    private boolean isStateMatch(Object val, WaitingListState targetState) {
        if (val == null) return false;

        String stateStr = "";

        if (val instanceof String) {
            stateStr = (String) val;
        } else if (val instanceof Map) {
            Map<?, ?> vMap = (Map<?, ?>) val;
            // Check 'second' (from Pair), 'state', or 'status'
            Object s = vMap.get("second");
            if (s == null) s = vMap.get("state");
            if (s == null) s = vMap.get("status");
            if (s != null) stateStr = s.toString();
        }

        return stateStr.equalsIgnoreCase(targetState.name());
    }

    @Override
    public CompletableFuture<List<String>> getEntrantIds(String eventId, Group group) {
        WaitingListState targetState;
        switch (group) {
            case SELECTED:
                targetState = WaitingListState.SELECTED;
                break;
            case CANCELLED:
                // CRITICAL: Matches WaitingListState.CANCELED (One 'L')
                targetState = WaitingListState.CANCELED;
                break;
            case ATTENDING:
                // Matches WaitingListState.ACCEPTED
                targetState = WaitingListState.ACCEPTED;
                break;
            default:
                targetState = WaitingListState.ENTERED;
                break;
        }
        return getEntrantsByState(eventId, targetState);
    }

    @Override
    public CompletableFuture<List<String>> filterOptedIn(String eventId, List<String> candidateUserIds) {
        return CompletableFuture.completedFuture(candidateUserIds);
    }
}