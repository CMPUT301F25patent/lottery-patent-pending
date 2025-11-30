package com.example.lotterypatentpending;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lotterypatentpending.models.AdminLogPresenter;
import com.example.lotterypatentpending.models.NotificationLog;

/**
 * RecyclerView adapter for admin notification logs.
 * Uses {@link AdminLogPresenter} to format:
 *  - a title line (category + event id)
 *  - a body preview (truncated)
 *  - meta info (time + organizer id)
 * Clicks are delegated to the {@link OnClick} callback so the Activity
 * can show full details in a dialog.
 *
 * @author Moffat
 * @maintainer Moffat
 */
public class AdminLogAdapter extends ListAdapter<NotificationLog, AdminLogAdapter.Holder> {

    /**
     * Callback for when a log row is tapped.
     */
    public interface OnClick {
        void open(NotificationLog log);
    }

    private final OnClick onClick;

    public AdminLogAdapter(@NonNull OnClick onClick) {
        super(DIFF);
        this.onClick = onClick;
    }

    /**
     * DiffUtil callback so RecyclerView can animate updates efficiently.
     */
    private static final DiffUtil.ItemCallback<NotificationLog> DIFF =
            new DiffUtil.ItemCallback<NotificationLog>() {
                @Override
                public boolean areItemsTheSame(@NonNull NotificationLog oldItem,
                                               @NonNull NotificationLog newItem) {
                    // We don't have an explicit id field, so approximate identity
                    // with (organizerId, eventId, createdAt).
                    if (oldItem.getCreatedAt() == null || newItem.getCreatedAt() == null) {
                        return false;
                    }
                    return oldItem.getOrganizerId().equals(newItem.getOrganizerId())
                            && oldItem.getEventId().equals(newItem.getEventId())
                            && oldItem.getCreatedAt().equals(newItem.getCreatedAt());
                }

                @Override
                public boolean areContentsTheSame(@NonNull NotificationLog oldItem,
                                                  @NonNull NotificationLog newItem) {
                    // If your NotificationLog gets equals()/hashCode() later, you can just use that.
                    return oldItem.getOrganizerId().equals(newItem.getOrganizerId())
                            && oldItem.getEventId().equals(newItem.getEventId())
                            && oldItem.getCategory().equals(newItem.getCategory())
                            && safeEquals(oldItem.getPayloadPreview(), newItem.getPayloadPreview())
                            && safeEquals(oldItem.getRecipientIds(), newItem.getRecipientIds())
                            && safeEquals(oldItem.getCreatedAt(), newItem.getCreatedAt());
                }

                private boolean safeEquals(Object a, Object b) {
                    if (a == b) return true;
                    if (a == null || b == null) return false;
                    return a.equals(b);
                }
            };

    static class Holder extends RecyclerView.ViewHolder {
        final TextView title;
        final TextView body;
        final TextView meta;

        Holder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            body  = itemView.findViewById(R.id.body);
            meta  = itemView.findViewById(R.id.meta);
        }
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View row = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_notif, parent, false);
        return new Holder(row);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        NotificationLog log = getItem(position);

        // Use the presenter for all formatting
        holder.title.setText(AdminLogPresenter.formatTitle(log));
        holder.body.setText(AdminLogPresenter.formatBody(log));
        holder.meta.setText(AdminLogPresenter.formatMeta(log));

        holder.itemView.setOnClickListener(v -> {
            if (onClick != null) {
                onClick.open(log);
            }
        });
    }
}
