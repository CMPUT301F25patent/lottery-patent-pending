package com.example.lotterypatentpending;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import androidx.core.util.Pair;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lotterypatentpending.adapters.EventListAdapter;
import com.example.lotterypatentpending.helpers.DateTimeFormatHelper;
import com.example.lotterypatentpending.helpers.DateTimePickerHelper;
import com.example.lotterypatentpending.helpers.LoadingOverlay;
import com.example.lotterypatentpending.helpers.TagDropdownHelper;
import com.example.lotterypatentpending.models.Event;
import com.example.lotterypatentpending.models.FirebaseManager;
import com.example.lotterypatentpending.models.User;
import com.example.lotterypatentpending.models.WaitingListState;
import com.example.lotterypatentpending.viewModels.UserEventRepository;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ListenerRegistration;

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

    private ListenerRegistration eventsListener;

    private String filterTag = null;

    // State of the three switches (popup)
    private boolean filterAll = true;          // default ON
    private boolean filterWaitlisted = false;  // default OFF
    private boolean filterAccepted = false;    // default OFF
    private String currentUserId = null;

    // Master lists
    private final ArrayList<Event> allEventsList = new ArrayList<>();
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

        // Get current user ID for waiting list filters
        User currentUser = userEventRepo.getUser().getValue();
        if (currentUser != null) {
            currentUserId = currentUser.getUserId();
        }

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

        // Load events from Firebase as LIVE data
        eventsListener = fm.getAllEventsLive(new FirebaseManager.FirebaseCallback<ArrayList<Event>>() {
            @Override
            public void onSuccess(ArrayList<Event> result) {
                if (!isAdded()) return;

                List<Event> safe = (result == null) ? new ArrayList<>() : result;
                allEventsList.clear();
                allEventsList.addAll(safe);

                historyMode = false;
                updateModeButtons(browseEventsBtn, historyBtn);
                applyFilter(getQuery(searchInput));   // rebuild shownEventsList
                if (loading != null) loading.hide();
            }

            @Override
            public void onFailure(Exception e) {
                if (!isAdded()) return;

                shownEventsList.clear();
                eventsListAdapter.notifyDataSetChanged();
                if (loading != null) loading.hide();
            }
        });

        // Click handler for the end icon
        //Search filter attach listener
        til.setEndIconOnClickListener(v -> {
            filterWindowPopup(v);
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
            getPastEvents(getQuery(searchInput));
        });

        // Visual default: Browse selected
        historyMode = false;
        updateModeButtons(browseEventsBtn, historyBtn);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (eventsListener != null) {
            eventsListener.remove();
            eventsListener = null;
        }
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
    /** Filter current base (browse/history) by query (case-insensitive). */
    private void applyFilter(String query) {
        String q = (query == null) ? "" : query.toLowerCase().trim();
        List<Event> base = historyMode ? historyEventsList : allEventsList;

        shownEventsList.clear();

        // If All is ON, or no specific state filter is ON,
        // we DO NOT apply any waiting-list filtering.
        boolean useStateFilter = !filterAll && (filterWaitlisted || filterAccepted);

        for (Event e : base) {
            // 1) Search bar: ONLY title
            if (!matchesText(e.getTitle(), q)) continue;

            // 2) Date filter
            if (!overlaps(e.getDate(), filterStartTime, filterEndTime))
                continue;

            // 3) Tag filter from popup
            if (filterTag != null && !filterTag.isEmpty()) {
                String eventTag = e.getTag();
                if (eventTag == null || !eventTag.equalsIgnoreCase(filterTag)) {
                    continue;
                }
            }

            // 4) Waiting list state filter (All / Waitlisted / Entered)
            if (useStateFilter) {
                WaitingListState state = getUserWaitingListState(e);
                boolean include = false;

                // Waitlisted -> any state EXCEPT NOT_IN
                if (filterWaitlisted && state != WaitingListState.NOT_IN) {
                    include = true;
                }

                // Entered/Accepted -> ACCEPTED
                if (filterAccepted && state == WaitingListState.ACCEPTED) {
                    include = true;
                }

                if (!include) continue;
            }
            // If !useStateFilter, behave like "All" (no state filter)

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

    private static boolean matchesText(@Nullable String title, String query) {

        if (query == null || query.isEmpty()) return true;

        query = query.toLowerCase();

        return title != null && title.toLowerCase().contains(query);
    }


    /**
     *
     * @param anchor will be anchored to view
     * this is the pop-up for the filter being clicked
     */
    private void filterWindowPopup(View anchor) {


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
        AutoCompleteTextView tagFilterDropdown = content.findViewById(R.id.tagFilterDropdown);
        SwitchMaterial swAll = content.findViewById(R.id.browseAllEvents);
        SwitchMaterial swWaitlisted = content.findViewById(R.id.browseWaitListEvents);
        SwitchMaterial swAccepted = content.findViewById(R.id.browseEnteredEvents);



        // Restore current toggle state when opening popup
        swAll.setChecked(filterAll);
        swWaitlisted.setChecked(filterWaitlisted);
        swAccepted.setChecked(filterAccepted);


        //Set dropdown
        TagDropdownHelper.setupTagDropdown(requireContext(), tagFilterDropdown, fm);
        // Prefill selected tag if user chose one before
        if (filterTag != null && !filterTag.isEmpty()) {
            tagFilterDropdown.setText(filterTag, false);
        }

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

        // All: when turned ON, turn others OFF
        swAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            filterAll = isChecked;
            if (isChecked) {
                // turn off the other filters
                filterWaitlisted = false;
                filterAccepted = false;
                swWaitlisted.setChecked(false);
                swAccepted.setChecked(false);
            }
            // if user turns All OFF manually, we just leave it off;
            // applyFilter will decide what that means based on other toggles
        });

        // Waitlisted: when turned ON, turn All OFF
        swWaitlisted.setOnCheckedChangeListener((buttonView, isChecked) -> {
            filterWaitlisted = isChecked;
            if (isChecked) {
                filterAll = false;
                swAll.setChecked(false);
            }
        });

        // Accepted: when turned ON, turn All OFF
        swAccepted.setOnCheckedChangeListener((buttonView, isChecked) -> {
            filterAccepted = isChecked;
            if (isChecked) {
                filterAll = false;
                swAll.setChecked(false);
            }
        });

        // When popup closes, save state + reapply filter
        filterPopup.setOnDismissListener(() -> {
            filterStartTime = DateTimeFormatHelper.parseTimestamp(startDate.getText().toString());
            filterEndTime = DateTimeFormatHelper.parseTimestamp(endDate.getText().toString());

            String chosenTag = tagFilterDropdown.getText().toString().trim();
            filterTag = chosenTag.isEmpty() ? null : chosenTag;

            // In case user changed switches right before dismiss
            filterAll = swAll.isChecked();
            filterWaitlisted = swWaitlisted.isChecked();
            filterAccepted = swAccepted.isChecked();

            TextInputEditText searchText = requireView().findViewById(R.id.searchInput);
            applyFilter(getQuery(searchText));
        });

        clear.setOnClickListener(v -> {
            // clear dates
            startDate.setText("");
            endDate.setText("");

            filterStartTime = null;
            filterEndTime = null;

            // clear tag filter
            tagFilterDropdown.setText("");
            filterTag = null;

            // Reset switches: only All = true
            filterAll = true;
            filterWaitlisted = false;
            filterAccepted = false;
            swAll.setChecked(true);
            swWaitlisted.setChecked(false);
            swAccepted.setChecked(false);

            // clear search
            TextInputEditText searchText = requireView().findViewById(R.id.searchInput);
            searchText.setText("");
            applyFilter(getQuery(searchText));

            if (filterPopup != null) filterPopup.dismiss();
        });

        // anchor under icon
        filterPopup.showAsDropDown(anchor, 0, 0);
    }

    private void getPastEvents(String searchInput) {
        User user = userEventRepo.getUser().getValue();
        assert user != null;
        fm.getUserPastEvents(user, new FirebaseManager.FirebaseCallback<List<Event>>() {
            @Override
            public void onSuccess(List<Event> result) {
                historyEventsList.clear();
                if (result != null) {
                    historyEventsList.addAll(result);
                }

                // Apply the filter
                applyFilter(searchInput);

                if (historyEventsList.isEmpty()) {
                    Toast.makeText(getContext(), "No past events found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Exception e) {
                historyEventsList.clear();
                applyFilter(searchInput);
                Toast.makeText(getContext(), "Failed to load history: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private WaitingListState getUserWaitingListState(@NonNull Event event) {
        if (currentUserId == null ||
                event.getWaitingList() == null ||
                event.getWaitingList().getList() == null) {
            return WaitingListState.NOT_IN;
        }

        for (Pair<User, WaitingListState> entry : event.getWaitingList().getList()) {
            User u = entry.first;
            WaitingListState state = entry.second;

            if (u != null && currentUserId.equals(u.getUserId())) {
                return (state != null) ? state : WaitingListState.NOT_IN;
            }
        }

        return WaitingListState.NOT_IN;
    }


}