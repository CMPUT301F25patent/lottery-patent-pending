package com.example.lotterypatentpending.User_interface.Inbox;

import android.view.*; import android.widget.TextView;
import androidx.annotation.NonNull; import androidx.recyclerview.widget.*;
import com.example.lotterypatentpending.models.Notification;
import java.text.SimpleDateFormat; import java.util.Locale;
import com.example.lotterypatentpending.R;

/**
 * RecyclerView adapter for user notifications.
 *
 * <p>Highlights unread items (alpha 1f) and dims read ones (alpha ~0.55f).
 * Clicking an item invokes the provided callback; caller typically calls
 * {@code repo.markRead(userId, n.getId())}.
 *
 * @author Moffat
 * @maintainer Moffat
 */

public class NotificationAdapter extends ListAdapter<Notification, NotificationAdapter.Holder> {
    interface OnClick { void open(Notification n); }
    private final OnClick onClick;
    public NotificationAdapter(OnClick onClick){ super(DIFF); this.onClick=onClick; }

    static DiffUtil.ItemCallback<Notification> DIFF = new DiffUtil.ItemCallback<>() {
        public boolean areItemsTheSame(@NonNull Notification a,@NonNull Notification b){ return a.equals(b); }
        public boolean areContentsTheSame(@NonNull Notification a,@NonNull Notification b){ return a.equals(b); }
    };

    static class Holder extends RecyclerView.ViewHolder {
        TextView title, body, meta; Holder(View v){ super(v);
            title=v.findViewById(R.id.title); body=v.findViewById(R.id.body); meta=v.findViewById(R.id.meta);}
    }

    @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup p,int v){
        return new Holder(LayoutInflater.from(p.getContext()).inflate(R.layout.item_notification,p,false));}
    @Override public void onBindViewHolder(@NonNull Holder h,int pos){
        var n=getItem(pos);
        h.title.setText(n.getTitle());
        h.body.setText(n.getBody());
        String ts = n.getCreatedAt()==null? "" : new SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(n.getCreatedAt());
        h.meta.setText(n.getCategory()+" • "+ts);
        h.itemView.setAlpha(n.isRead()?0.55f:1f);
        h.itemView.setOnClickListener(v -> onClick.open(n));
    }
}

