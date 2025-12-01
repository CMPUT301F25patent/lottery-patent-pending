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
     * Callback used when a notification row is tapped.
     * The Activity decides what to do -- show full message and mark read.
     */
    interface OnClick { void open(Notification n); }
    private final OnClick onClick;
    public NotificationAdapter(OnClick onClick){ super(DIFF); this.onClick=onClick; }

    static DiffUtil.ItemCallback<Notification> DIFF = new DiffUtil.ItemCallback<>() {
        public boolean areItemsTheSame(@NonNull Notification a, @NonNull Notification b) {
            return a.getId() != null && a.getId().equals(b.getId());
        }
        public boolean areContentsTheSame(@NonNull Notification a,@NonNull Notification b){ return a.equals(b); }
    };

    static class Holder extends RecyclerView.ViewHolder {
        TextView title, body, meta, readBadge;
        Holder(View v){
            super(v);
            title=v.findViewById(R.id.title);
            body=v.findViewById(R.id.body);
            meta=v.findViewById(R.id.meta);
            readBadge = v.findViewById(R.id.readBadge);}
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent,int viewType){
        View row = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new Holder(row);}
    @Override
    public void onBindViewHolder(@NonNull Holder h, int pos){
        Notification n = getItem(pos);
        h.title.setText(n.getTitle());
        // Body preview (truncate to 1–2 lines)
        String preview = n.getBody();
        if (preview.length() > 80) preview = preview.substring(0, 77) + "…";
        h.body.setText(preview);

        String ts = n.getCreatedAt() == null ? "" :
                new SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(n.getCreatedAt());
        h.meta.setText(n.getCategory() + " • " + ts);

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

        h.itemView.setOnClickListener(v -> {
            onClick.open(n);
        });
    }
}

