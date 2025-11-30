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
 * - a title line (category + event id)
 * - a body preview (truncated)
 * - meta info (time + organizer id)
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
        /**
         * Called when a log item is clicked.
         * @param log The {@link NotificationLog} that was clicked.
         */
        void open(NotificationLog log);
    }

    /** The click listener implemented by the host (Activity/Fragment). */
    private final OnClick onClick;

    /**
     * Constructs the adapter, providing the click listener.
     * @param onClick The {@link OnClick} listener.
     */
    public AdminLogAdapter(@NonNull OnClick onClick) {
        super(DIFF);
        this.onClick = onClick;
    }

    /**
     * DiffUtil callback for efficient list updates.
     */
    private static final DiffUtil.ItemCallback<NotificationLog> DIFF =
            new DiffUtil.ItemCallback<NotificationLog>() {
                /**
                 * Checks if two items represent the same logical entity.
                 */
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

                /**
                 * Checks if the content of two items is the same.
                 */
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

                /** Helper method for safe comparison of potentially null objects. */
                private boolean safeEquals(Object a, Object b) {
                    if (a == b) return true;
                    if (a == null || b == null) return false;
                    return a.equals(b);
                }
            };

    /**
     * ViewHolder for a single item row in the RecyclerView.
     */
    static class Holder extends RecyclerView.ViewHolder {
        /** TextView for the main title line. */
        final TextView title;
        /** TextView for the body/payload preview. */
        final TextView body;
        /** TextView for the metadata (time, organizer ID). */
        final TextView meta;

        /**
         * Constructs the ViewHolder and finds its views.
         * @param itemView The root view of the item layout.
         */
        Holder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            body  = itemView.findViewById(R.id.body);
            meta  = itemView.findViewById(R.id.meta);
        }
    }

    /**
     * Creates new ViewHolder instances.
     */
    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View row = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_notif, parent, false);
        return new Holder(row);
    }

    /**
     * Binds the {@link NotificationLog} data to the views in the ViewHolder.
     * Formatting is delegated to {@link AdminLogPresenter}.
     * @param holder The ViewHolder to update.
     * @param position The position of the item.
     */
    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        NotificationLog log = getItem(position);

        // Use the presenter for all formatting
        holder.title.setText(AdminLogPresenter.formatTitle(log));
        holder.body.setText(AdminLogPresenter.formatBody(log));
        holder.meta.setText(AdminLogPresenter.formatMeta(log));

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (onClick != null) {
                onClick.open(log);
            }
        });
    }
}