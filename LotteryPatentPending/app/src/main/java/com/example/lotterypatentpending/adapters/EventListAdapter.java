package com.example.lotterypatentpending.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.lotterypatentpending.R;
import com.example.lotterypatentpending.helpers.DateTimeFormatHelper;
import com.example.lotterypatentpending.models.Event;

import java.util.List;

/**
 * author: Erik
 * contributor: Erik
 *
 * to add more features simply add more TextViews to item_event and here
 */
public class EventListAdapter extends ArrayAdapter<Event> {

    public EventListAdapter(@NonNull Context context, @NonNull List<Event> events){
        super(context, 0, events);
    }

    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent){

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_event, parent, false);
        }

        Event event = getItem(position);

        TextView name = convertView.findViewById(R.id.eventName);
        TextView location = convertView.findViewById(R.id.eventLocation);
        TextView eventTime = convertView.findViewById(R.id.eventTime);
        TextView regTime = convertView.findViewById(R.id.regTime);

        if (event != null) {
            name.setText(event.getTitle());
            String locationText = event.getLocation() != null && !event.getLocation().isEmpty() ?
                    event.getLocation() : "Not set";
            location.setText(locationText);
            String formattedTime = DateTimeFormatHelper.formatTimestamp(event.getDate());
            eventTime.setText(formattedTime);
            regTime.setText(event.getFormattedRegWindow());
        }

        return convertView;
    }
}
