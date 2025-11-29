package com.example.lotterypatentpending;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.lotterypatentpending.models.AdminLogPresenter;
import com.example.lotterypatentpending.models.FirestoreAdminLogRepository;
import com.example.lotterypatentpending.models.NotificationLog;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.ArrayList;
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
        adapter = new AdminNotifAdapter();
        recycler.setAdapter(adapter);

        swipe.setOnRefreshListener(this::refresh);
        firstLoad();
    }

    /**
     * Performs the initial one-shot load of notification logs.
     * Shows a spinner while loading, then displays the list or empty state.
     */
    private void firstLoad() {
        showLoading(true);
        repo.getAllLogs()
                .thenAccept(list -> runOnUiThread(() -> {
                    showLoading(false);
                    applyList(list);
                }))
                .exceptionally(e -> {
                    runOnUiThread(() -> {
                        showLoading(false);
                        applyList(new ArrayList<>()); // fall back to empty
                    });
                    return null;
                });
    }

    /** Pull-to-refresh handler. */
    private void refresh() {
        repo.getAllLogs()
                .thenAccept(list -> runOnUiThread(() -> {
                    swipe.setRefreshing(false);
                    applyList(list);
                }))
                .exceptionally(e -> {
                    runOnUiThread(() -> swipe.setRefreshing(false));
                    return null;
                });
    }
    /**
     * Updates the RecyclerView with the provided list of logs and
     * toggles visibility between the list and the empty state view.
     *
     * @param items The list of NotificationLog entries to display.
     */
    private void applyList(List<NotificationLog> items) {
        if (items == null) items = new ArrayList<>();
        adapter.submit(items);
        boolean isEmpty = items.isEmpty();
        recycler.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        emptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }
    /**
     * Controls the visibility of the loading spinner and temporarily hides
     * list/empty views while data is being fetched.
     *
     * @param show If true, display the loading spinner; otherwise show content.
     */
    private void showLoading(boolean show) {
        progress.setVisibility(show ? View.VISIBLE : View.GONE);
        recycler.setVisibility(show ? View.GONE : View.VISIBLE);
        emptyState.setVisibility(View.GONE);
    }

    /** Minimal adapter that renders NotificationLog using item_admin_notif.xml. */
    private static final class AdminNotifAdapter extends RecyclerView.Adapter<AdminNotifAdapter.VH> {
        private final List<NotificationLog> data = new ArrayList<>();

        static final class VH extends RecyclerView.ViewHolder {
            final TextView title, body, meta;
            VH(@NonNull View v) {
                super(v);
                title = v.findViewById(R.id.title);
                body  = v.findViewById(R.id.body);
                meta  = v.findViewById(R.id.meta);
            }
        }
        /**
         * Replaces the adapter’s dataset with new items and refreshes the UI.
         *
         * @param items The new list of NotificationLog entries.
         */
        void submit(@NonNull List<NotificationLog> items) {
            data.clear();
            data.addAll(items);
            notifyDataSetChanged();
        }

        @NonNull
        @Override public VH onCreateViewHolder(@NonNull android.view.ViewGroup parent, int viewType) {
            View row = android.view.LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_admin_notif, parent, false);
            return new VH(row);
        }

        @Override public void onBindViewHolder(@NonNull VH h, int pos) {
            NotificationLog n = data.get(pos);
            h.title.setText(AdminLogPresenter.formatTitle(n));
            h.body.setText(AdminLogPresenter.formatBody(n));
            h.meta.setText(AdminLogPresenter.formatMeta(n));
        }

        @Override public int getItemCount() { return data.size(); }
    }
}

