package com.example.lotterypatentpending.data;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Firestore-backed implementation of {@link UserDataSource}.
 * - Entrant groups are subcollections of events/{eventId}/(...)
 * - Opt-in is read from users/{uid}.preferences.notificationsOptIn (Boolean) or users/{uid}.notificationsOptIn
 */
public class FirestoreUsersDataSource implements UserDataSource {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    public CompletableFuture<List<String>> getEntrantIds(String eventId, Group group) {
        final CompletableFuture<List<String>> f = new CompletableFuture<>();

        String sub;
        switch (group) {
            case WAITLIST:
                sub = "waitingList";
                break;
            case SELECTED:
                sub = "selected";
                break;
            case CANCELLED:
                sub = "cancelled";
                break;
            default:
                sub = "waitingList";
                break;
        }

        db.collection("events")
                .document(eventId)
                .collection(sub)
                .get()
                .addOnSuccessListener((QuerySnapshot snap) -> {
                    List<String> ids = new ArrayList<>();
                    for (DocumentSnapshot d : snap.getDocuments()) {
                        String uid = d.getString("userId");
                        if (uid != null) ids.add(uid);
                    }
                    f.complete(ids);
                })
                .addOnFailureListener(f::completeExceptionally);

        return f;
    }

    @Override
    public CompletableFuture<List<String>> filterOptedIn(String eventId, List<String> candidateUserIds) {
        final CompletableFuture<List<String>> f = new CompletableFuture<>();

        if (candidateUserIds == null || candidateUserIds.isEmpty()) {
            f.complete(new ArrayList<String>());
            return f;
        }

        final List<String> optedIn = new ArrayList<>();
        final int chunkSize = 10; // Firestore whereIn limit
        final int totalChunks = (candidateUserIds.size() + chunkSize - 1) / chunkSize;
        final AtomicInteger remaining = new AtomicInteger(totalChunks);

        for (int i = 0; i < candidateUserIds.size(); i += chunkSize) {
            final List<String> chunk = candidateUserIds.subList(i, Math.min(i + chunkSize, candidateUserIds.size()));

            db.collection("users")
                    .whereIn(FieldPath.documentId(), new ArrayList<>(chunk))
                    .get()
                    .addOnSuccessListener(snap -> {
                        for (DocumentSnapshot d : snap.getDocuments()) {
                            // Read opt-in from either users/{uid}.preferences.notificationsOptIn or users/{uid}.notificationsOptIn
                            Boolean opt = null;

                            // nested map: preferences.notificationsOptIn
                            Map<String, Object> prefs = d.get("preferences", Map.class);
                            if (prefs != null && prefs.get("notificationsOptIn") instanceof Boolean) {
                                opt = (Boolean) prefs.get("notificationsOptIn");
                            }

                            // flat fallback: notificationsOptIn
                            if (opt == null) {
                                Boolean flat = d.getBoolean("notificationsOptIn");
                                if (flat != null) opt = flat;
                            }

                            if (opt != null && opt) {
                                optedIn.add(d.getId());
                            }
                        }
                        if (remaining.decrementAndGet() == 0 && !f.isDone()) {
                            f.complete(optedIn);
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Fail fast on first error
                        if (!f.isDone()) f.completeExceptionally(e);
                    });
        }

        return f;
    }
}
