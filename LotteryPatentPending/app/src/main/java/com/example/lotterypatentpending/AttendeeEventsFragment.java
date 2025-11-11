package com.example.lotterypatentpending;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.example.lotterypatentpending.adapters.EventListAdapter;
import com.example.lotterypatentpending.models.Event;
import com.example.lotterypatentpending.models.FirebaseManager;
import com.example.lotterypatentpending.viewModels.UserEventRepository;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class AttendeeEventsFragment extends Fragment {
    private UserEventRepository userEventRepo;
    private FirebaseManager fm;

    // Master lists
    private final ArrayList<Event> allEventsList = new ArrayList<>();
    // TODO: implement  history
    private final ArrayList<Event> historyEventsList = new ArrayList<>();

    // What the ListView shows
    private final ArrayList<Event> shownEventsList = new ArrayList<>();
    private EventListAdapter eventsListAdapter; //custom adapter
    // false = Browse (default), true = History
    private boolean historyMode = false;

    public AttendeeEventsFragment() {
        super(R.layout.attendee_fragment_events);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userEventRepo = UserEventRepository.getInstance();
        fm = FirebaseManager.getInstance();

        ListView eventsListView       = view.findViewById(R.id.attendee_events_listview_events_list);
        TextInputEditText searchInput = view.findViewById(R.id.searchInput);
        Button searchBtn              = view.findViewById(R.id.btn_search);
        Button browseEventsBtn        = view.findViewById(R.id.attendee_events_button_browse_events);
        Button historyBtn             = view.findViewById(R.id.attendee_events_button_event_history);


        eventsListAdapter = new EventListAdapter(
                requireContext(),
                shownEventsList
        );

        //Set adapter to the list of Events
        eventsListView.setAdapter(eventsListAdapter);

        // Load events from Firebase safely
        fm.getAllEvents(new FirebaseManager.FirebaseCallback<ArrayList<Event>>() {
            @Override
            public void onSuccess(ArrayList<Event> result) {
                List<Event> safe = (result == null) ? new ArrayList<>() : result;
                allEventsList.clear();
                allEventsList.addAll(safe);

                historyMode = false;
                updateModeButtons(browseEventsBtn, historyBtn);
                applyFilter(getQuery(searchInput));
            }

            @Override
            public void onFailure(Exception e) {
                // Keep lists empty;
                shownEventsList.clear();
                eventsListAdapter.notifyDataSetChanged();
            }
        });

        // Click -> open details
        eventsListView.setOnItemClickListener((parent, v1, position, id) -> {
            if (position < 0 || position >= shownEventsList.size()) return;
            Event selectedEvent = shownEventsList.get(position);
            userEventRepo.setEvent(selectedEvent);

            AttendeeEventDetailsFragment fragment = new AttendeeEventDetailsFragment();
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.attendeeContainer, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        // Search button: run filter once
        if (searchBtn != null) {
            searchBtn.setOnClickListener(v -> applyFilter(getQuery(searchInput)));
        }

        // Keyboard Search / Enter: run filter
        if (searchInput != null) {
            searchInput.setOnEditorActionListener((TextView v1, int actionId, KeyEvent event) -> {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    applyFilter(getQuery(searchInput));
                    return true; // handled
                }
                return false;
            });
        }


        // Mode buttons
        browseEventsBtn.setOnClickListener(v -> {
            historyMode = false;
            updateModeButtons(browseEventsBtn, historyBtn);
            applyFilter(getQuery(searchInput));
        });

        historyBtn.setOnClickListener(v -> {
            historyMode = true;
            updateModeButtons(browseEventsBtn, historyBtn);
            applyFilter(getQuery(searchInput));
        });

        // Visual default: Browse selected
        historyMode = false;
        updateModeButtons(browseEventsBtn, historyBtn);
    }

    private void updateModeButtons(Button browseBtn, Button historyBtn) {
        // simple “selected = disabled” look
        browseBtn.setEnabled(historyMode);     // if showing history, enable Browse
        historyBtn.setEnabled(!historyMode);   // if showing browse, enable History
    }

    private String getQuery(@Nullable TextInputEditText et) {
        if (et == null || et.getText() == null) return "";
        return et.getText().toString();
    }

    /** Filter current base (browse/history) by query (case-insensitive). */
    private void applyFilter(String query) {
        String q = (query == null) ? "" : query.toLowerCase().trim();
        List<Event> base = historyMode ? historyEventsList : allEventsList;

        shownEventsList.clear();


        for (Event e : base) {
            // Use the Event's title
            String title = (e.getTitle() == null) ? "" : e.getTitle();

            if (q.isEmpty() || title.toLowerCase().contains(q)) {
                shownEventsList.add(e);
            }
        }

        eventsListAdapter.notifyDataSetChanged();
    }
}