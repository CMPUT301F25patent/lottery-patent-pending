package com.example.lotterypatentpending;

import android.os.Bundle;
import android.view.View;




import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;


import com.example.lotterypatentpending.models.FirestoreAdminLogRepository;
import com.example.lotterypatentpending.models.NotificationLog;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.firestore.ListenerRegistration;


import java.util.List;

/**
 * Displays the Notification audit log for admins.
 * <p>Layout: {@code activity_admin_notif.xml}.
 * <ul>
 * <li>Toolbar: {@code @id/toolbar}</li>
 * <li>Pull-to-refresh: {@code @id/swipe}</li>
 * <li>List: {@code @id/recycler}</li>
 * <li>Empty state: {@code @id/emptyState}</li>
 * <li>Spinner: {@code @id/progress}</li>
 * </ul>
 * Data: {@link FirestoreAdminLogRepository#getAllLogs()} (one-shot). Pull to refresh re-runs it.
 * @author Moffat
 * @maintainer Moffat
 */
public class NotificationAdminActivity extends AppCompatActivity {

    /** The {@link SwipeRefreshLayout} for pull-to-refresh functionality. */
    private SwipeRefreshLayout swipe;
    /** The {@link RecyclerView} to display the notification logs. */
    private RecyclerView recycler;
    /** The view shown when the log list is empty. */
    private View emptyState;
    /** The progress indicator shown during data loading. */
    private CircularProgressIndicator progress;

    /** Repository for accessing notification log data in Firestore. */
    private final FirestoreAdminLogRepository repo = new FirestoreAdminLogRepository();
    /** Adapter for binding {@link NotificationLog} data to the {@link RecyclerView}. */
    private AdminLogAdapter adapter;
    /** Registration object for the real-time Firestore listener. */
    private ListenerRegistration logsReg;

    /**
     * Initializes the activity, sets up the toolbar, RecyclerView, and initiates
     * the real-time listener for notification logs.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_notif);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        swipe = findViewById(R.id.swipe);
        recycler = findViewById(R.id.recycler);
        emptyState = findViewById(R.id.emptyState);
        progress = findViewById(R.id.progress);

        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminLogAdapter(log -> showLogDetailsDialog(log));
        recycler.setAdapter(adapter);

        // initial load + optional live listener
        progress.setVisibility(View.VISIBLE);
        logsReg = repo.listenAllLogs(new FirestoreAdminLogRepository.LogListener() {
            @Override
            public void onChanged(List<NotificationLog> logs) {
                runOnUiThread(() -> {
                    progress.setVisibility(View.GONE);
                    adapter.submitList(logs);
                    emptyState.setVisibility(logs == null || logs.isEmpty()
                            ? View.VISIBLE : View.GONE);
                    swipe.setRefreshing(false);
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    progress.setVisibility(View.GONE);
                    swipe.setRefreshing(false);
                    android.util.Log.e("AdminLogs", "listenAllLogs", e);
                });
            }
        });

        swipe.setOnRefreshListener(() -> {
            // Simple refresh: re-trigger Firestore using getAllLogs()
            repo.getAllLogs()
                    .thenAccept(logs -> runOnUiThread(() -> {
                        adapter.submitList(logs);
                        emptyState.setVisibility(logs == null || logs.isEmpty()
                                ? View.VISIBLE : View.GONE);
                        swipe.setRefreshing(false);
                    }))
                    .exceptionally(e -> {
                        runOnUiThread(() -> swipe.setRefreshing(false));
                        return null;
                    });
        });
    }

    /**
     * Displays an {@link androidx.appcompat.app.AlertDialog} containing detailed information
     * about the selected notification log.
     * @param log The {@link NotificationLog} to display details for.
     */
    private void showLogDetailsDialog(NotificationLog log) {
        StringBuilder sb = new StringBuilder();
        sb.append("Category: ").append(log.getCategory()).append("\n\n")
                .append("Event: ").append(log.getEventId()).append("\n")
                .append("Organizer: ").append(log.getOrganizerId()).append("\n\n")
                .append("Recipients: ").append(
                        log.getRecipientIds() == null ? "0" : log.getRecipientIds().size()
                ).append("\n\n")
                .append("Body preview:\n")
                .append(log.getPayloadPreview() == null ? "(none)" : log.getPayloadPreview());

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Notification details")
                .setMessage(sb.toString())
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    /**
     * Stops the real-time Firestore listener when the activity is no longer visible.
     */
    @Override
    protected void onStop() {
        super.onStop();
        if (logsReg != null) {
            logsReg.remove();
            logsReg = null;
        }
    }
}