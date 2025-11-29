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
 *   <li>Toolbar: {@code @id/toolbar}</li>
 *   <li>Pull-to-refresh: {@code @id/swipe}</li>
 *   <li>List: {@code @id/recycler}</li>
 *   <li>Empty state: {@code @id/emptyState}</li>
 *   <li>Spinner: {@code @id/progress}</li>
 * </ul>
 * Data: {@link FirestoreAdminLogRepository#getAllLogs()} (one-shot). Pull to refresh re-runs it.
 *
 * @author Moffat
 * @maintainer Moffat
 */
public class NotificationAdminActivity extends AppCompatActivity {

    private SwipeRefreshLayout swipe;
    private RecyclerView recycler;
    private View emptyState;
    private CircularProgressIndicator progress;

    private final FirestoreAdminLogRepository repo = new FirestoreAdminLogRepository();
    private AdminNotifAdapter adapter;
    private ListenerRegistration logsReg;
    /**
     * Initializes the admin notification log screen, sets up UI components,
     * configures pull-to-refresh behavior, and performs the initial data load.
     *
     * @param savedInstanceState Previously saved state (unused).
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
    @Override
    protected void onStop() {
        super.onStop();
        if (logsReg != null) {
            logsReg.remove();
            logsReg = null;
        }
    }
}

