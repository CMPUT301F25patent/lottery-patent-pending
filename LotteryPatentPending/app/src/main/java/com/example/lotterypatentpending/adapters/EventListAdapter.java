package com.example.lotterypatentpending.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.example.lotterypatentpending.R;
import com.example.lotterypatentpending.helpers.DateTimeFormatHelper;
import com.example.lotterypatentpending.models.Event;
import com.example.lotterypatentpending.viewModels.EventViewModel;

import java.util.List;

/**
 * author: Erik
 * contributor: Erik
 *
 * to add more features simply add more TextViews to item_event and here
 */
public class EventListAdapter extends ArrayAdapter<Event> {

    public interface OnEventActionListener {
        void onEdit(Event event);
        void onDelete(Event event);
    }

    private OnEventActionListener listener;

    public EventListAdapter(@NonNull Context context, @NonNull List<Event> events){
        super(context, 0, events);
    }

    public EventListAdapter(@NonNull Context context, @NonNull List<Event> events,
                            OnEventActionListener listener) {
        super(context, 0, events);
        this.listener = listener;
    }

    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent){

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_event, parent, false);
        }

        Event event = getItem(position);

        TextView name = convertView.findViewById(R.id.eventName);
        TextView tag = convertView.findViewById(R.id.eventTag);
        TextView location = convertView.findViewById(R.id.eventLocation);
        TextView eventTime = convertView.findViewById(R.id.eventTime);
        TextView regTime = convertView.findViewById(R.id.regTime);
        ImageButton editBtn = convertView.findViewById(R.id.btnEdit);
        ImageButton deleteBtn = convertView.findViewById(R.id.btnDelete);

        if (event != null) {
            name.setText(event.getTitle());
            tag.setText(event.getTag());

            //use helper to convert Timestamp of UTC time to local time(string)
            String locationText = event.getLocation() != null && !event.getLocation().isEmpty() ?
                    event.getLocation() : "Not set";
            location.setText(locationText);
            String formattedTime = DateTimeFormatHelper.formatTimestamp(event.getDate());
            eventTime.setText(formattedTime);
            regTime.setText(event.getFormattedRegWindow());

            editBtn.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEdit(event);
                }
            });

            deleteBtn.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDelete(event);
                }
            });

        }

        return convertView;
    }
}
