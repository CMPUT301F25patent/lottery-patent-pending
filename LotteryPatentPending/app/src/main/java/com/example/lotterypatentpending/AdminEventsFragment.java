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

    private FirebaseManager firebaseManager;

    private TextInputEditText searchInput;
    private MaterialButton searchButton;
    private ListView listView;

    // all events from Firestore
    private final ArrayList<Event> allEvents = new ArrayList<>();
    // events currently shown (after filtering)
    private final ArrayList<Event> visibleEvents = new ArrayList<>();

    private ListenerRegistration eventsListener;

    private EventListAdapter eventListAdapter;
    private LoadingOverlay loading;

    public AdminEventsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.admin_fragment_events, container, false);
    }

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

    private void refreshList() {
        eventListAdapter.notifyDataSetChanged();
    }

    /**
     * Deletes an event from Firestore and reloads the list.
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
     * Loads all events from Firestore and populates allEvents + visibleEvents.
     */
    private void loadEventsFromFirebase() {
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (eventsListener != null) {
            eventsListener.remove();
            eventsListener = null;
        }
    }

    /**
     * Filters visibleEvents by title using the text in the search bar.
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
        for (Event e : allEvents) {
            String title = e.getTitle();
            if (title != null &&
                    title.toLowerCase().contains(query.toLowerCase())) {
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
