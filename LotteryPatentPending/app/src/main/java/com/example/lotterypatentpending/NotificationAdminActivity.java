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

    /** First load shows a spinner, then swaps to list/empty state. */
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

    private void applyList(List<NotificationLog> items) {
        if (items == null) items = new ArrayList<>();
        adapter.submit(items);
        boolean isEmpty = items.isEmpty();
        recycler.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        emptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

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

