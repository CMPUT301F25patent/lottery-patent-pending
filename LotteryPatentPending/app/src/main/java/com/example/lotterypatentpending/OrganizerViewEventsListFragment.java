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
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.lotterypatentpending.adapters.EventListAdapter;
import com.example.lotterypatentpending.helpers.LoadingOverlay;
import com.example.lotterypatentpending.models.Event;
import com.example.lotterypatentpending.models.FirebaseManager;
import com.example.lotterypatentpending.models.User;
import com.example.lotterypatentpending.viewModels.EventViewModel;
import com.example.lotterypatentpending.viewModels.UserEventRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.EventListener;

/**
 * Fragment allowing organizers to view their event list (future
 * and search for a specific event by ID. (only functionality included so far)
 * Displays a list of events created by the current organizer, supports searching
 * by event name, and provides options to view, edit, or delete events.
 * @author
 * @contributor Erik
 */

public class OrganizerViewEventsListFragment extends Fragment {


    /** Input field for searching events by title. */
    private TextInputEditText searchInput;
    /** Button to trigger the search functionality. */
    private MaterialButton searchButton;
    /** ListView to display the filtered list of events. */
    private ListView listView;
    /** Singleton instance of {@link FirebaseManager} for data operations. */
    private FirebaseManager fm;
    /** Adapter for binding event data to the {@link ListView}. */
    private EventListAdapter eventListAdapter;
    // all events from Firestore
    /** List containing all events organized by the current user, fetched from Firestore. */
    private final ArrayList<Event> allEvents = new ArrayList<>();
    // events currently shown in the list (after filtering)
    /** List of events currently visible in the ListView (a filtered subset of {@link #allEvents}). */
    private final ArrayList<Event> visibleEvents = new ArrayList<>();
    /** Controller for showing and hiding the loading spinner overlay. */
    private LoadingOverlay loading;
    /** Firestore listener to keep the list of organized events synchronized in real-time. */
    private ListenerRegistration organizedEventsListener;


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
     * Initializes UI elements, sets up the list adapter, attaches the Firestore listener
     * to load organized events, and sets click listeners for searching and list items.
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
        eventListAdapter = new EventListAdapter(
                requireContext(),
                visibleEvents,
                new EventListAdapter.OnEventActionListener() {

                    @Override
                    public void onEdit(Event event) {
                        // 1. Put the event into the shared ViewModel
                        EventViewModel viewModel = new ViewModelProvider(requireActivity()).get(EventViewModel.class);
                        viewModel.setEvent(event);

                        Bundle bundle = new Bundle();
                        bundle.putBoolean("isEdit", true);

                        // 2. Navigate to your EventView/Edit fragment
                        NavHostFragment.findNavController(OrganizerViewEventsListFragment.this)
                                .navigate(R.id.action_viewEventsList_to_Edit_Event_view, bundle);
                    }

                    @Override
                    public void onDelete(Event event) {
                        new MaterialAlertDialogBuilder(requireContext())
                                .setTitle("Delete Event")
                                .setMessage("Are you sure you want to delete \"" + event.getTitle() + "\"?")
                                .setPositiveButton("Delete", (dialog, which) -> {
                                    deleteEvent(event);
                                })
                                .setNegativeButton("Cancel", null)
                                .show();
                    }
                });

        listView.setAdapter(eventListAdapter);

        // show spinner while loading Firestore data
        loading.show();

        organizedEventsListener = fm.getOrganizedEvents(userId,
                new FirebaseManager.FirebaseCallback<ArrayList<Event>>() {
                    @Override
                    public void onSuccess(ArrayList<Event> events) {
                        if (!isAdded()) return;

                        allEvents.clear();
                        if (events != null) {
                            allEvents.addAll(events);
                        }

                        visibleEvents.clear();
                        visibleEvents.addAll(allEvents);
                        refreshListFromVisible();

                        if (loading != null) loading.hide();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e("ViewOrgEvents", "Failed to load organized events", e);

                        if (!isAdded()) return;

                        visibleEvents.clear();
                        refreshListFromVisible();

                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Failed to load events", Toast.LENGTH_SHORT).show();
                        }
                        if (loading != null) loading.hide();
                    }
                });

        // Clicking a row opens that event (from filtered list)
        listView.setOnItemClickListener((parent, view, position, id) -> {
            if (position < 0 || position >= visibleEvents.size()) return;

            Event clicked = visibleEvents.get(position);
            EventViewModel evm =
                    new ViewModelProvider(requireActivity()).get(EventViewModel.class);
            evm.setEvent(clicked);

            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_viewEventsList_to_Event_View);
        });

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

    /**
     * Called when the view hierarchy is being destroyed.
     * Removes the Firestore listener to prevent memory leaks.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (organizedEventsListener != null) {
            organizedEventsListener.remove();
            organizedEventsListener = null;
        }
    }

    /**
     * Notifies the {@link EventListAdapter} that the dataset in {@link #visibleEvents} has changed,
     * triggering a list refresh.
     */
    private void refreshListFromVisible(){
        eventListAdapter.notifyDataSetChanged();
    }


    /**
     * Filters the {@link #allEvents} list based on the text entered in the search field
     * (matching against the event title), updates {@link #visibleEvents}, and refreshes the list.
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

    /**
     * Deletes the specified event from the local list and from Firestore.
     * @param event The event to be deleted.
     */
    public void deleteEvent(Event event){
        visibleEvents.remove(event);
        fm.deleteEvent(event.getId());
        refreshListFromVisible();
    }

}