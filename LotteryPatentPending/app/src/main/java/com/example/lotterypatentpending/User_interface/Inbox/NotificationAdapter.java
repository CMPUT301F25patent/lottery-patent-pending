package com.example.lotterypatentpending.User_interface.Inbox;


import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lotterypatentpending.R;
import com.example.lotterypatentpending.models.Notification;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.VH> {

    // click callback used by your Activity/Fragment
    public interface OnItemClick { void onClick(Notification n); }

    private final OnItemClick onItemClick;
    private List<Notification> items = new ArrayList<>();

    public NotificationAdapter(@NonNull OnItemClick onItemClick) {
        this.onItemClick = onItemClick;
    }

    public void submit(@NonNull List<Notification> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Notification n = items.get(position);
        h.title.setText(n.getTitle());
        h.body.setText(n.getBody());

        if (n.getCreatedAt() != null) {
            h.time.setText(
                    DateFormat.getDateTimeInstance().format(n.getCreatedAt().toDate())
            );
        } else {
            h.time.setText("");
        }

        boolean isRead = "READ".equals(n.getStatus());
        h.title.setTypeface(null, isRead ? Typeface.NORMAL : Typeface.BOLD);

        h.itemView.setOnClickListener(v -> onItemClick.onClick(n));
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        final TextView title, body, time;
        VH(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            body  = itemView.findViewById(R.id.body);
            time  = itemView.findViewById(R.id.time);
        }
    }
}

