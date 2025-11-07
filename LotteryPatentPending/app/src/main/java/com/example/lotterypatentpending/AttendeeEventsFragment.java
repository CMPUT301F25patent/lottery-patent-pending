package com.example.lotterypatentpending;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.example.lotterypatentpending.models.Event;
import com.example.lotterypatentpending.models.FirebaseManager;
import com.example.lotterypatentpending.models.UserEventRepository;

import java.util.ArrayList;

/**
 * Fragment that displays a list of events available to attendees.
 * Allows searching, browsing, and selecting an event to view details.
 *
 * This fragment is the default landing screen for attendees, showing
 * available events fetched from Firebase.
 */
public class AttendeeEventsFragment extends Fragment {
    private UserEventRepository userEventRepo;
    private FirebaseManager fm;
    /** List containing all events fetched from Firestore. */
    private ArrayList<Event> allEventsList;

    /** List containing events currently shown in the UI (may be filtered). */
    private ArrayList<Event> shownEventsList;

    /** Adapter backing the event ListView. */
    private ArrayAdapter<Event> eventsListAdapter;

    /**
     * Default constructor inflating the attendee events list layout.
     */
    public AttendeeEventsFragment() {
        super(R.layout.fragment_attendee_events);
    }

    /**
     * Initializes the event list UI, fetches events from Firestore,
     * and sets up handlers for search and event selection.
     *
     * @param view the fragment view
     * @param savedInstanceState saved state, if any
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userEventRepo = UserEventRepository.getInstance();
        fm = FirebaseManager.getInstance();

        // get all events from Firebase
        allEventsList = new ArrayList<>();
        fm.getAllEvents(new FirebaseManager.FirebaseCallback<ArrayList<Event>>() {
            @Override
            public void onSuccess(ArrayList<Event> result) {
                allEventsList = result;
            }

            @Override
            public void onFailure(Exception e) {
                // TODO
            }
        });

        // initially, show all events
        shownEventsList = new ArrayList<>();
        shownEventsList.addAll(allEventsList);

        eventsListAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, shownEventsList);

        ListView eventsListView = view.findViewById(R.id.attendee_events_listview_events_list);
        EditText searchInput = view.findViewById(R.id.attendee_events_edittext_search);
        Button searchButton = view.findViewById(R.id.attendee_events_button_search);
        Button browseEvents = view.findViewById(R.id.attendee_events_button_browse_events);
        Button eventHistory = view.findViewById(R.id.attendee_events_button_event_history);

        eventsListView.setAdapter(eventsListAdapter);

        eventsListView.setOnItemClickListener((parent, view1, position, id) -> {
            Event selectedEvent = shownEventsList.get(position);
            userEventRepo.setEvent(selectedEvent);
            AttendeeEventDetailsFragment fragment = new AttendeeEventDetailsFragment();
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.attendeeContainer, fragment)
                    .addToBackStack(null)
                    .commit();
        });
        searchButton.setOnClickListener(v -> {
            String query = searchInput.getText().toString().trim();
            if (!query.isEmpty()) {
                performSearch(query);
            }
        });


    }
    /**
     * Filters events based on the user search query.
     * (Method stub — implementation pending)
     *
     * @param query the text search query
     */
    private void performSearch(String query) {

    }
}