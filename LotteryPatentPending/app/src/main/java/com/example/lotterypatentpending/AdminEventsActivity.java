package com.example.lotterypatentpending;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lotterypatentpending.models.Event;
import com.example.lotterypatentpending.models.FirebaseManager;
import com.example.lotterypatentpending.models.User;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;


/**
 * AdminEventsActivity allows administrators to view and manage all events
 * stored in Firebase. The admin can see event details and delete events
 * using a long-press action.
 *
 * This is an internal admin-panel screen not accessible to standard users.
 */
public class AdminEventsActivity extends AppCompatActivity {

    /** Firebase manager instance used to interact with database. */
    private FirebaseManager firebaseManager;

    /** ListView UI element to display events. */
    private ListView listView;

    /** Adapter used to bind event text data to the ListView. */
    private ArrayAdapter<String> adapter;

    /** List of actual Event objects returned from Firestore. */
    private List<Event> eventList = new ArrayList<>();

    /** List of formatted event strings to show in UI. */
    private List<String> eventDisplayList = new ArrayList<>();


    /**
     * Called when the activity is created. Sets UI, initializes Firebase,
     * loads events, and assigns listeners for deletion and navigation.
     *
     * @param savedInstanceState previous activity state, if any.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_events);

        listView = findViewById(R.id.eventListView);
        firebaseManager = FirebaseManager.getInstance();

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, eventDisplayList);
        listView.setAdapter(adapter);

        loadEventsFromFirebase();
        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());



        // Long press → delete event
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            Event selectedEvent = eventList.get(position);

            new AlertDialog.Builder(AdminEventsActivity.this)
                    .setTitle("Delete Event")
                    .setMessage("Are you sure you want to delete the event \"" + selectedEvent.getTitle() + "\"?")
                    .setPositiveButton("Delete", (dialog, which) -> removeEvent(selectedEvent))
                    .setNegativeButton("Cancel", null)
                    .show();

            return true;
        });
    }


    /**
     * Deletes a selected event from Firebase and refreshes the UI list.
     *
     * @param event the event to remove.
     */
    private void removeEvent(Event event) {
                    FirebaseManager.getInstance().deleteEvent(event.getId());
                    Toast.makeText(this, "Deleted event: " + event.getTitle(), Toast.LENGTH_SHORT).show();
                    // Refresh UI
                    loadEventsFromFirebase();
                    adapter.notifyDataSetChanged();

    }


    /**
     * Retrieves all events from Firebase and populates the ListView with
     * formatted display strings. On failure, logs an error.
     */
    private void loadEventsFromFirebase() {
        firebaseManager.getAllEvents(new FirebaseManager.FirebaseCallback<ArrayList<Event>>() {
            @Override
            public void onSuccess(ArrayList<Event> result) {
                eventList.clear();
                eventDisplayList.clear();

                for (Event event : result) {
                    if (event == null) continue;


                    String organizerName = (event.getOrganizer() != null)
                            ? event.getOrganizer().getName()
                            : "Unknown Organizer";

                    String display = event.getTitle() +
                            "\nBy: " + organizerName +
                            "\n" + event.getDescription() +
                            "\nLocation: " + event.getLocation() +
                            " | Capacity: " + event.getCapacity();

                    eventDisplayList.add(display);
                    eventList.add(event);

                }

                adapter.notifyDataSetChanged();
                Log.d("BrowseEvents", "Loaded " + eventList.size() + " events.");
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("BrowseEvents", "Error loading events: " + e.getMessage());
            }
        });
    }


}