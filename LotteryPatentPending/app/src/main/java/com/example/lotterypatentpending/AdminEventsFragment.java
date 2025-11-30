package com.example.lotterypatentpending;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.lotterypatentpending.adapters.EventListAdapter;
import com.example.lotterypatentpending.helpers.LoadingOverlay;
import com.example.lotterypatentpending.models.Event;
import com.example.lotterypatentpending.models.FirebaseManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;


/**
 * Fragment allowing administrators to browse and manage all events.
 * Admin version of the organizer events list with search + delete + loading screen.
 */
public class AdminEventsFragment extends Fragment {

    /** Instance of the Firebase data manager. */
    private FirebaseManager firebaseManager;

    /** Input field for searching/filtering events. */
    private TextInputEditText searchInput;
    /** Button to trigger the search/filter operation. */
    private MaterialButton searchButton;
    /** ListView to display the events. */
    private ListView listView;

    /** List containing all events fetched from Firestore. */
    private final ArrayList<Event> allEvents = new ArrayList<>();
    /** List containing events currently displayed to the user (filtered subset of allEvents). */
    private final ArrayList<Event> visibleEvents = new ArrayList<>();

    /** Registration object for the real-time listener on all events. */
    private ListenerRegistration eventsListener;

    /** Adapter to link event data to the ListView. */
    private EventListAdapter eventListAdapter;
    /** Utility for showing a full-screen loading indicator. */
    private LoadingOverlay loading;

    /**
     * Required empty public constructor.
     */
    public AdminEventsFragment() {
        // Required empty public constructor
    }

    /**
     * Inflates the layout for this fragment.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.admin_fragment_events, container, false);
    }

    /**
     * Initializes views, sets up the adapter, loading overlay, and listeners for search and delete.
     */
    @Override
    public void onViewCreated(@NonNull View v,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        // Views
        searchInput = v.findViewById(R.id.searchInput);
        searchButton = v.findViewById(R.id.btn_search);
        listView = v.findViewById(R.id.eventListView);

        firebaseManager = FirebaseManager.getInstance();

        // Attach loading overlay (same pattern as OrganizerViewEventsListFragment)
        ViewGroup root = v.findViewById(R.id.admin_events_root);
        View overlayView = getLayoutInflater().inflate(
                R.layout.loading_screen,
                root,
                false
        );
        root.addView(overlayView);
        loading = new LoadingOverlay(overlayView, null);

        // Set up adapter with visibleEvents
        eventListAdapter = new EventListAdapter(requireContext(), visibleEvents);
        listView.setAdapter(eventListAdapter);

        // Load all events initially
        loading.show();
        loadEventsFromFirebase();

        // Search button click
        searchButton.setOnClickListener(view -> filterEvents());

        // Keyboard search / enter
        searchInput.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH
                    || actionId == EditorInfo.IME_ACTION_DONE
                    || (keyEvent != null
                    && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER
                    && keyEvent.getAction() == KeyEvent.ACTION_DOWN)) {

                filterEvents();
                return true;
            }
            return false;
        });

        // Long press → delete event (from filtered list)
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            if (position < 0 || position >= visibleEvents.size()) return true;

            Event selectedEvent = visibleEvents.get(position);

            new AlertDialog.Builder(requireContext())
                    .setTitle("Delete Event")
                    .setMessage("Are you sure you want to delete the event \"" +
                            selectedEvent.getTitle() + "\"?")
                    .setPositiveButton("Delete", (dialog, which) -> removeEvent(selectedEvent))
                    .setNegativeButton("Cancel", null)
                    .show();

            return true;
        });
    }

    /**
     * Notifies the adapter that the data set has changed, causing the ListView to refresh.
     */
    private void refreshList() {
        eventListAdapter.notifyDataSetChanged();
    }

    /**
     * Deletes a specified event from Firestore and reloads the entire list to reflect the change.
     * @param event The {@link Event} object to be deleted.
     */
    private void removeEvent(Event event) {
        loading.show();
        firebaseManager.deleteEvent(event.getId());
        Toast.makeText(requireContext(),
                "Deleted event: " + event.getTitle(),
                Toast.LENGTH_SHORT).show();
        // Reload from Firestore so both allEvents and visibleEvents stay in sync
        loadEventsFromFirebase();
    }

    /**
     * Establishes a real-time listener to load all events from Firestore.
     * The results populate both {@code allEvents} and {@code visibleEvents}.
     */
    private void loadEventsFromFirebase() {
        // Remove existing listener if present
        if (eventsListener != null) {
            eventsListener.remove();
        }

        eventsListener = firebaseManager.getAllEventsLive(new FirebaseManager.FirebaseCallback<ArrayList<Event>>() {
            @Override
            public void onSuccess(ArrayList<Event> result) {
                if (!isAdded()) return;

                allEvents.clear();
                visibleEvents.clear();

                if (result != null) {
                    allEvents.addAll(result);
                    visibleEvents.addAll(result);
                }

                refreshList();
                if (loading != null) loading.hide();

                Log.d("AdminEventsFragment", "Loaded " + visibleEvents.size() + " events.");
            }

            @Override
            public void onFailure(Exception e) {
                if (!isAdded()) return;
                Log.e("AdminEventsFragment", "Error loading events: " + e.getMessage());
                if (loading != null) loading.hide();

                Toast.makeText(requireContext(),
                        "Failed to load events", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Called when the view hierarchy is being destroyed.
     * The real-time Firestore listener is removed here to prevent memory leaks.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (eventsListener != null) {
            eventsListener.remove();
            eventsListener = null;
        }
    }

    /**
     * Filters the {@code allEvents} list based on the text currently in the search bar.
     * The filtered results are placed into {@code visibleEvents} and the list is refreshed.
     */
    private void filterEvents() {
        String query = "";
        if (searchInput.getText() != null) {
            query = searchInput.getText().toString().trim();
        }

        if (query.isEmpty()) {
            visibleEvents.clear();
            visibleEvents.addAll(allEvents);
            refreshList();
            Toast.makeText(requireContext(),
                    "Showing all events", Toast.LENGTH_SHORT).show();
            return;
        }

        ArrayList<Event> matches = new ArrayList<>();
        String lowerQuery = query.toLowerCase();
        for (Event e : allEvents) {
            String title = e.getTitle();
            if (title != null && title.toLowerCase().contains(lowerQuery)) {
                matches.add(e);
            }
        }

        if (matches.isEmpty()) {
            Toast.makeText(requireContext(),
                    "No events match that name", Toast.LENGTH_SHORT).show();
        }

        visibleEvents.clear();
        visibleEvents.addAll(matches);
        refreshList();
    }
}