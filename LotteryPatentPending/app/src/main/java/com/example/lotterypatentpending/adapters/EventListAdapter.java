package com.example.lotterypatentpending.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.lotterypatentpending.R;
import com.example.lotterypatentpending.models.Event;

import java.util.List;

/**
 * author: Erik
 * contributor: Erik
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

        TextView nameTV = convertView.findViewById(R.id.eventName);
        TextView locationTV = convertView.findViewById(R.id.eventLocation);
        TextView timeTV = convertView.findViewById(R.id.eventTime);

        if (event != null) {
            nameTV.setText(event.getTitle());
            locationTV.setText(event.getLocation());
            timeTV.setText(event.getFormattedRegWindow());
        }

        return convertView;
    }
}
