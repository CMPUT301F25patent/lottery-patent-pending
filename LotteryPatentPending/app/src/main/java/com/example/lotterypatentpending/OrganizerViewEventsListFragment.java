package com.example.lotterypatentpending;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.lotterypatentpending.adapters.EventListAdapter;
import com.example.lotterypatentpending.helpers.LoadingOverlay;
import com.example.lotterypatentpending.models.Event;
import com.example.lotterypatentpending.models.FirebaseManager;
import com.example.lotterypatentpending.models.User;
import com.example.lotterypatentpending.viewModels.UserEventRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

/**
 * Fragment allowing organizers to view their event list (future
 * and search for a specific event by ID. (only functionality included so far)
 * @author
 * @contributor Erik
 */

public class OrganizerViewEventsListFragment extends Fragment {


    private TextInputEditText searchInput;
    private MaterialButton searchButton;
    private ListView listView;
    private FirebaseManager fm;
    private EventListAdapter eventListAdapter;
    // all events from Firestore
    private final ArrayList<Event> allEvents = new ArrayList<>();
    // events currently shown in the list (after filtering)
    private final ArrayList<Event> visibleEvents = new ArrayList<>();
    private LoadingOverlay loading;


    /**
     * Inflates the fragment's layout.
     *
     * @param inflater           The LayoutInflater object that can be used to inflate any views.
     * @param container          The parent ViewGroup.
     * @param savedInstanceState Bundle containing saved instance state.
     * @return The root View of the fragment's layout.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.organizer_fragment_view_organizer_events_list, container, false);
    }

    /**
     * Called after the view has been created.
     * Initializes UI elements, sets click listeners for buttons.
     *
     * @param v                  The root view of the fragment.
     * @param savedInstanceState Bundle containing saved instance state.
     */
    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        searchInput = v.findViewById(R.id.searchInput);
        searchButton = v.findViewById(R.id.btn_search);
        listView = v.findViewById(R.id.organizerEventsList);

        fm = FirebaseManager.getInstance();

        // Attach loading screen
        ViewGroup root = v.findViewById(R.id.organizer_events_root);
        View overlayView = getLayoutInflater().inflate(
                R.layout.loading_screen,
                root,
                false);

        // Add overlayView to root
        root.addView(overlayView);

        // Adds loading screen controller
        loading = new LoadingOverlay(overlayView, null);

        User currentUser = UserEventRepository.getInstance().getUser().getValue();
        if (currentUser == null) {
            Log.e("ViewOrganizerEventsList", "Current user is null, cannot load events");
            return; // or show a message / navigate away
        }

        //get current userId from repo
        String userId = currentUser.getUserId();

        //Setup adapter with events
        eventListAdapter = new EventListAdapter(requireContext(), visibleEvents);
        listView.setAdapter(eventListAdapter);

        // show spinner while loading Firestore data
        loading.show();

        fm.getOrganizedEventsOnce(userId, new FirebaseManager.FirebaseCallback<ArrayList<Event>>() {
            @Override
            public void onSuccess(ArrayList<Event> events) {
                if (!isAdded()) return;
                allEvents.clear();
                allEvents.addAll(events);


                visibleEvents.clear();
                visibleEvents.addAll(events);
                refreshListFromVisible();
                if (loading != null) loading.hide();

            }

            @Override
            public void onFailure(Exception e) {
                Log.e("ViewOrgEvents", "Failed to load organized events", e);

                if (!isAdded()) return;

                if (getContext() != null) {
                    Toast.makeText(getContext(), "Failed to load events", Toast.LENGTH_SHORT).show();
                }
                if (loading != null) loading.hide();
            }
        });

//        TODO: implement view event
//    // Clicking a row opens that event (from filtered list)
//        listView.setOnItemClickListener((parent, view, position, id) -> {
//        if (position < 0 || position >= visibleEvents.size()) return;
//
//        Event clicked = visibleEvents.get(position);
//        EventViewModel evm =
//                new ViewModelProvider(requireActivity()).get(EventViewModel.class);
//        evm.setEvent(clicked);
//
//        NavHostFragment.findNavController(this)
//                .navigate(R.id.action_viewEventsList_to_Event_View);
//    });

        //Click search button
        searchButton.setOnClickListener(view ->
            getSearchedEvent());

        // Press Enter / Search on keyboard
        searchInput.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH
                    || actionId == EditorInfo.IME_ACTION_DONE
                    || (keyEvent != null
                    && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER
                    && keyEvent.getAction() == KeyEvent.ACTION_DOWN)) {

                getSearchedEvent();
                return true; // we handled it
            }
            return false;
        });
    }

    private void refreshListFromVisible(){
        eventListAdapter.notifyDataSetChanged();
    }


    /**
     * Retrieves an event from Firebase based on the text entered
     * in the search field and stores it in the shared EventViewModel.
     */
    public void getSearchedEvent(){
        String query = "";
        if (searchInput.getText() != null) {
            query = searchInput.getText().toString().trim();
        }

        if (query.isEmpty()){
            visibleEvents.clear();
            visibleEvents.addAll(allEvents);
            refreshListFromVisible();
            Toast.makeText(requireContext(), "Showing all events", Toast.LENGTH_SHORT).show();
            return;
        }

        ArrayList<Event> matches = new ArrayList<>();
        for (Event e: allEvents) {
            String title = e.getTitle();
            if (title != null &&
                title.toLowerCase().contains(query.toLowerCase())){
                matches.add(e);
            }
        }

        if (matches.isEmpty()) {
            Toast.makeText(requireContext(), "No events match that name", Toast.LENGTH_SHORT).show();
        }
        visibleEvents.clear();
        visibleEvents.addAll(matches);
        refreshListFromVisible();
    }

}
