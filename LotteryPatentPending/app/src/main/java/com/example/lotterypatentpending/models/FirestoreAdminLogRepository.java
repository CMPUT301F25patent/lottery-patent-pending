package com.example.lotterypatentpending.models;

import androidx.annotation.Nullable;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * A repository class that implements the {@link AdminLogRepository} interface
 * using Google Firestore as the data source. This class is responsible for all
 * database operations related to administrator notification logs, such as
 * recording new logs and fetching existing ones.
 * <p>
 * The Firestore structure used is: /admin/notificationsLog/records/{logId}
 */
public class FirestoreAdminLogRepository implements AdminLogRepository {

    /** The root collection for all admin-related data. */
    private static final String ROOT = "admin";
    /** The specific document that contains the notification log sub-collection. */
    private static final String DOC  = "notificationsLog";
    /** The sub-collection where individual log records are stored. */
    private static final String SUB  = "records";

    /** An instance of the FirebaseFirestore client. */
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    /**
     * Records a new {@link NotificationLog} to the Firestore database.
     *
     * @param log The {@link NotificationLog} object to be saved.
     * @return A {@link CompletableFuture<Void>} that completes when the operation
     *         is successful, or completes exceptionally on failure.
     */
    @Override
    public CompletableFuture<Void> record(NotificationLog log) {
        CompletableFuture<Void> f = new CompletableFuture<>();
        db.collection(ROOT).document(DOC).collection(SUB)
                .add(log)
                .addOnSuccessListener(r -> f.complete(null))
                .addOnFailureListener(f::completeExceptionally);
        return f;
    }

    /** One-shot read, newest first. Used by AdminActivity.loadData(). */
    public CompletableFuture<List<NotificationLog>> getAllLogs() {
        CompletableFuture<List<NotificationLog>> f = new CompletableFuture<>();
        db.collection(ROOT).document(DOC).collection(SUB)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener((QuerySnapshot snap) -> {
                    List<NotificationLog> out = new ArrayList<>();
                    for (DocumentSnapshot d : snap.getDocuments()) {
                        NotificationLog row = d.toObject(NotificationLog.class);
                        if (row != null) {
                            // backfill createdAt if missing
                            if (row.getCreatedAt() == null) {
                                @Nullable Timestamp ts = d.getTimestamp("createdAt");
                                if (ts != null) row.setCreatedAt(ts.toDate());
                            }
                            out.add(row);
                        }
                    }
                    f.complete(out);
                })
                .addOnFailureListener(f::completeExceptionally);
        return f;
    }

    /** Optional: realtime listener for auto-updating UI. */
    public ListenerRegistration listenAllLogs(LogListener listener) {
        return db.collection(ROOT).document(DOC).collection(SUB)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snap, err) -> {
                    if (err != null) {
                        listener.onError(err);
                        return;
                    }
                    List<NotificationLog> out = new ArrayList<>();
                    if (snap != null) {
                        for (DocumentSnapshot d : snap.getDocuments()) {
                            NotificationLog row = d.toObject(NotificationLog.class);
                            if (row != null) {
                                if (row.getCreatedAt() == null) {
                                    Timestamp ts = d.getTimestamp("createdAt");
                                    if (ts != null) row.setCreatedAt(ts.toDate());
                                }
                                out.add(row);
                            }
                        }
                    }
                    listener.onChanged(out);
                });
    }

    /** Callback for realtime updates. */
    public interface LogListener {
        void onChanged(List<NotificationLog> logs);
        void onError(Exception e);
    }
}


