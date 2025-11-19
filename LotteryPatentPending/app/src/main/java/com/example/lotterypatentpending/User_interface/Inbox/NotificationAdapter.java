package com.example.lotterypatentpending.User_interface.Inbox;

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
        public boolean areItemsTheSame(@NonNull Notification a,@NonNull Notification b){ return a.equals(b); }
        public boolean areContentsTheSame(@NonNull Notification a,@NonNull Notification b){ return a.equals(b); }
    };

    static class Holder extends RecyclerView.ViewHolder {
        TextView title, body, meta;
        Holder(View v){
            super(v);
            title=v.findViewById(R.id.title);
            body=v.findViewById(R.id.body);
            meta=v.findViewById(R.id.meta);}
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent,int v){
        View row = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new Holder(row);}
    @Override public void onBindViewHolder(@NonNull Holder h,int pos){
        Notification n = getItem(pos);
        h.title.setText(n.getTitle());
        h.body.setText(n.getBody());
        String ts = n.getCreatedAt()==null? "" : new SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(n.getCreatedAt());
        h.meta.setText(n.getCategory()+" • "+ts);
        // Style based on read/unread
        if (n.isRead()) {
            // Read: normal weight, slightly dimmed
            h.title.setTypeface(null, Typeface.NORMAL);
            h.itemView.setAlpha(0.6f);
        } else {
            // Unread: bold title, full opacity
            h.title.setTypeface(null, Typeface.BOLD);
            h.itemView.setAlpha(1.0f);
        }
        h.itemView.setOnClickListener(v -> onClick.open(n));
    }
}

