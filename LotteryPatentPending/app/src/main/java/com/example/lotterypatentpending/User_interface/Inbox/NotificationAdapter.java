package com.example.lotterypatentpending.User_interface.Inbox;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.*; import android.widget.TextView;
import androidx.annotation.NonNull; import androidx.recyclerview.widget.*;
import com.example.lotterypatentpending.models.Notification;
import java.text.SimpleDateFormat; import java.util.Locale;
import com.example.lotterypatentpending.R;

/**
 * RecyclerView adapter for the user's inbox.
 * Renders Notification items with:
 * - title
 * - body preview
 * - category + timestamp meta
 * - visual difference between read/unread
 * @author Moffat
 * @maintainer Moffat
 */

public class NotificationAdapter extends ListAdapter<Notification, NotificationAdapter.Holder> {

    /**
     * Interface defining the callback for when a notification item is clicked.
     */
    interface OnClick {
        /**
         * Called when a notification row is tapped.
         * The activity (caller) should handle showing the full message and marking the notification as read.
         * @param n The {@link Notification} that was clicked.
         */
        void open(Notification n);
    }

    /** The callback instance to execute when a row is clicked. */
    private final OnClick onClick;

    /**
     * Constructs the adapter.
     * @param onClick The {@link OnClick} listener to handle row taps.
     */
    public NotificationAdapter(OnClick onClick){ super(DIFF); this.onClick=onClick; }

    /**
     * Differential callback used by {@link ListAdapter} to efficiently determine
     * which items in the list have changed.
     */
    static DiffUtil.ItemCallback<Notification> DIFF = new DiffUtil.ItemCallback<>() {
        /**
         * Checks if two items represent the same logical entity (based on Firestore ID).
         */
        public boolean areItemsTheSame(@NonNull Notification a, @NonNull Notification b) {
            return a.getId() != null && a.getId().equals(b.getId());
        }

        /**
         * Checks if the content of two items is the same (based on the {@code equals} method of {@link Notification}).
         */
        public boolean areContentsTheSame(@NonNull Notification a,@NonNull Notification b){ return a.equals(b); }
    };

    /**
     * ViewHolder class that holds references to the views for a single notification item row.
     */
    static class Holder extends RecyclerView.ViewHolder {
        /** TextView for the notification title. */
        TextView title;
        /** TextView for the truncated notification body preview. */
        TextView body;
        /** TextView for the category and timestamp metadata. */
        TextView meta;
        /** TextView acting as a visible badge for read status. */
        TextView readBadge;

        /**
         * Constructs the ViewHolder and finds all necessary views within the row layout.
         * @param v The root view of the item layout.
         */
        Holder(View v){
            super(v);
            title=v.findViewById(R.id.title);
            body=v.findViewById(R.id.body);
            meta=v.findViewById(R.id.meta);
            readBadge = v.findViewById(R.id.readBadge);}
    }

    /**
     * Creates new ViewHolder instances for the items in the RecyclerView.
     * @param parent The ViewGroup into which the new View will be added.
     * @param viewType The view type of the new View.
     * @return A new {@link Holder} instance.
     */
    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent,int viewType){
        View row = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new Holder(row);}

    /**
     * Binds the data of a specific {@link Notification} item to the views in a {@link Holder}.
     * This method handles formatting the body preview, timestamp, and setting the
     * visual state (read/unread) of the row.
     * @param h The {@link Holder} which should be updated to represent the contents of the item at the given position.
     * @param pos The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull Holder h, int pos){
        Notification n = getItem(pos);
        h.title.setText(n.getTitle());

        // Body preview (truncate to 1–2 lines)
        String preview = n.getBody();
        if (preview.length() > 80) preview = preview.substring(0, 77) + "…";
        h.body.setText(preview);

        // Format timestamp
        String ts = n.getCreatedAt() == null ? "" :
                new SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(n.getCreatedAt());
        h.meta.setText(n.getCategory() + " • " + ts);

        // ===== READ / UNREAD UI =====
        if (n.isRead()) {
            h.title.setTextColor(Color.parseColor("#B0B0B0"));
            h.body.setTextColor(Color.parseColor("#A6A6A6"));
            h.title.setTypeface(null, Typeface.NORMAL);
            h.itemView.setAlpha(0.75f);

            h.readBadge.setVisibility(View.VISIBLE);
            h.readBadge.setText("✓ Read");

        } else {
            h.title.setTextColor(Color.WHITE);
            h.body.setTextColor(Color.parseColor("#DDDDDD"));
            h.title.setTypeface(null, Typeface.BOLD);
            h.itemView.setAlpha(1f);

            h.readBadge.setVisibility(View.GONE);
        }

        // ===== CLICK =====
        h.itemView.setOnClickListener(v -> {
            onClick.open(n);

            // Show full message dialog
            new AlertDialog.Builder(v.getContext())
                    .setTitle(n.getTitle())
                    .setMessage(n.getBody())
                    .setPositiveButton("Close", null)
                    .show();
        });
    }
}