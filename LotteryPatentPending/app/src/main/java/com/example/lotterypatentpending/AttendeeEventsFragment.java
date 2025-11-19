package com.example.lotterypatentpending;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.example.lotterypatentpending.adapters.EventListAdapter;
import com.example.lotterypatentpending.helpers.DateTimeFormatHelper;
import com.example.lotterypatentpending.helpers.DateTimePickerHelper;
import com.example.lotterypatentpending.helpers.LoadingOverlay;
import com.example.lotterypatentpending.models.Event;
import com.example.lotterypatentpending.models.FirebaseManager;
import com.example.lotterypatentpending.viewModels.UserEventRepository;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Michael
 * @contributor Michael, Erik
 */

public class AttendeeEventsFragment extends Fragment {
    private UserEventRepository userEventRepo;
    private FirebaseManager fm;

    private LoadingOverlay loading;

    private PopupWindow filterPopup;

    private Timestamp filterStartTime;
    private Timestamp filterEndTime;

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

        //Search bar
        TextInputLayout til = view.findViewById(R.id.attendee_events_search_layout);
        //Search bar input
        TextInputEditText searchInput = view.findViewById(R.id.searchInput);


        ListView eventsListView = view.findViewById(R.id.attendee_events_listview_events_list);
        Button searchBtn = view.findViewById(R.id.btn_search);
        Button browseEventsBtn = view.findViewById(R.id.attendee_events_button_browse_events);
        Button historyBtn = view.findViewById(R.id.attendee_events_button_event_history);



        // Attach loading screen
        ViewGroup root = view.findViewById(R.id.attendee_events_root);
        View overlayView = getLayoutInflater().inflate(
                R.layout.loading_screen,
                root,
                false);

        root.addView(overlayView);
        loading = new LoadingOverlay(overlayView, null);

        eventsListAdapter = new EventListAdapter(
                requireContext(),
                shownEventsList
        );

        //Set adapter to the list of Events
        eventsListView.setAdapter(eventsListAdapter);

        loading.show();

        // Load events from Firebase safely
        fm.getAllEvents(new FirebaseManager.FirebaseCallback<ArrayList<Event>>() {
            @Override
            public void onSuccess(ArrayList<Event> result) {
                if (!isAdded()) return;

                List<Event> safe = (result == null) ? new ArrayList<>() : result;
                allEventsList.clear();
                allEventsList.addAll(safe);

                historyMode = false;
                updateModeButtons(browseEventsBtn, historyBtn);
                applyFilter(getQuery(searchInput));
                if (loading != null) loading.hide();
            }

            @Override
            public void onFailure(Exception e) {
                if (!isAdded()) return;

                // Keep lists empty;
                shownEventsList.clear();
                eventsListAdapter.notifyDataSetChanged();
                if (loading != null) loading.hide();

            }
        });

        // Click handler for the end icon
        //Search filter attach listener
        til.setEndIconOnClickListener(v -> {
            dateFilterPopup(v);
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
            if (!matchesText(e.getTitle(), q)) continue;

            // User range: [filterStartTime, filterEndTime]
            // Event start date
            //Return false whenever not in date, return true when within date
            if (!overlaps(e.getDate(), filterStartTime, filterEndTime))
                continue;
            shownEventsList.add(e);
        }

        eventsListAdapter.notifyDataSetChanged();
    }

    private static boolean overlaps(@Nullable Timestamp date, @Nullable Timestamp start, @Nullable Timestamp end) {
        //cannot filter empty event show all events.
        if (date == null) return true;

        if (start != null && date.compareTo(start) < 0) return false;
        if (end != null && date.compareTo(end) > 0) return false;

        return true;
    }

    private static boolean matchesText(@Nullable String title, String q) {

        if (q == null || q.isEmpty()) return true;

        return title != null && title.toLowerCase().contains(q);
    }


    private void dateFilterPopup(View anchor) {

        if (filterPopup != null && filterPopup.isShowing()){
            filterPopup.dismiss();
            return;
        }

        //Inflate popup layout
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View content = inflater.inflate(R.layout.attendee_popup_filter, null, false);

        filterPopup = new PopupWindow(
                content,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );

        filterPopup.setOutsideTouchable(true);
        filterPopup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView startDate = content.findViewById(R.id.startDate);
        TextView endDate = content.findViewById(R.id.endDate);
        TextView clear = content.findViewById(R.id.clearSearch);

        // Prefill the popup with any previously chosen range
        if (filterStartTime != null) {
            startDate.setText(DateTimeFormatHelper.formatTimestamp(filterStartTime));
        }
        if (filterEndTime != null) {
            endDate.setText(DateTimeFormatHelper.formatTimestamp(filterEndTime));
        }

        //Attach a date picker to each date textView
        DateTimePickerHelper.attachDateTimePicker(startDate, requireContext());
        DateTimePickerHelper.attachDateTimePicker(endDate, requireContext());


        filterPopup.setOnDismissListener(() -> {

            filterStartTime = DateTimeFormatHelper.parseTimestamp(startDate.getText().toString());
            filterEndTime = DateTimeFormatHelper.parseTimestamp(endDate.getText().toString());

            TextInputEditText searchText = requireView().findViewById(R.id.searchInput);
            applyFilter(getQuery(searchText));

        });

        clear.setOnClickListener(v -> {
            // clear stand and end dates
            startDate.setText("");
            endDate.setText("");

            filterStartTime = null;
            filterEndTime = null;

            // clear search
            TextInputEditText searchText = requireView().findViewById(R.id.searchInput);
            searchText.setText("");
            applyFilter(getQuery(searchText));

            // close popup
            if (filterPopup != null) filterPopup.dismiss();
        });

        // anchor under icon
        filterPopup.showAsDropDown(anchor, 0, 0);
    }
}