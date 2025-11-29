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
    /**
     * Callbacks for organizer actions (edit/delete).
     */
    public interface OnEventActionListener {
        void onEdit(Event event);
        void onDelete(Event event);
    }

    private final boolean showActions;

    private OnEventActionListener listener;
    /**
     * Creates a read-only adapter where events are shown
     * without edit/delete controls.
     *
     * @param context host Activity/Fragment
     * @param events  list of events to display
     */
    public EventListAdapter(@NonNull Context context, @NonNull List<Event> events){
        super(context, 0, events);
        this.showActions = false;
    }
    /**
     * Creates an adapter with organizer controls enabled.
     * Edit/delete buttons are shown and routed to {@code listener}.
     *
     * @param context  host Activity/Fragment
     * @param events   list of events to display
     * @param listener callback for edit/delete actions
     */
    public EventListAdapter(@NonNull Context context, @NonNull List<Event> events,
                            OnEventActionListener listener) {
        super(context, 0, events);
        this.listener = listener;
        this.showActions = true;
    }
    /**
     * Inflates and binds a row for an {@link Event}.
     *
     * <p>Populates text fields, formats timestamps, computes waiting-list
     * usage, and conditionally shows edit/delete buttons.</p>
     *
     * @param position    row index
     * @param convertView recycled view if available
     * @param parent      ListView container
     * @return the populated row view
     */
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
        TextView waitlist = convertView.findViewById(R.id.eventWaitlist);
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

            //  Compute waiting list text: "X / Y" or "N/A"
            int wlCap = event.getWaitingListCapacity();

            int currentSize = 0;
            if (event.getWaitingList() != null &&
                    event.getWaitingList().getList() != null) {
                currentSize = event.getWaitingList().getList().size();
            }

            String wlText;
            if (wlCap == -1) {
                wlText = "N/A";
            } else {
                wlText =  currentSize + " / " + wlCap;
            }
            waitlist.setText(wlText);

            if (!showActions) {
                // Any fragment that used the 2-arg constructor:
                // hide buttons completely
                editBtn.setVisibility(View.GONE);
                deleteBtn.setVisibility(View.GONE);
            } else {
                // Only OrganizerViewEventsListFragment uses this path
                editBtn.setVisibility(View.VISIBLE);
                deleteBtn.setVisibility(View.VISIBLE);

                editBtn.setOnClickListener(v -> {
                    if (listener != null) listener.onEdit(event);
                });

                deleteBtn.setOnClickListener(v -> {
                    if (listener != null) listener.onDelete(event);
                });
            }

        }

        return convertView;
    }
}
