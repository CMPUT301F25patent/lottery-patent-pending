/**
 * -----------------------------------------------------------------------------
 * FILE: AdminEventsActivity.java
 * PROJECT: Lottery Patent Pending
 * -----------------------------------------------------------------------------
 * PURPOSE:
 *   Allows administrators to view, delete, and manage events stored in Firebase.
 *   Fetches event data through FirebaseManager and displays them in a ListView.
 *
 * DESIGN ROLE / PATTERN:
 *   Acts as the View-Controller for event management, delegating data operations
 *   to FirebaseManager while handling user interactions.
 *
 * OUTSTANDING ISSUES / LIMITATIONS:
 *   - Event deletion depends on Firestore document ID synchronization.
 *   - No inline event editing implemented yet.
 *
 * AUTHOR: Ritvik Das
 * COLLABORATORS:
 * -----------------------------------------------------------------------------
 */

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
 * The {@code AdminEventsActivity} class allows administrators to browse and
 * manage all events in the system. Events are loaded from Firestore using
 * {@link FirebaseManager} and displayed in a {@link ListView}. Admins can
 * remove events directly from this interface.
 */
public class AdminEventsActivity extends AppCompatActivity {

    private FirebaseManager firebaseManager;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<Event> eventList = new ArrayList<>();
    private List<String> eventDisplayList = new ArrayList<>();
    /**
     * Initializes the event management screen and sets up the back button and event list.
     *
     * @param savedInstanceState previously saved state of the activity, if any.
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
     * Deletes an event from Firestore and updates the list view.
     *
     * @param event the {@link Event} to delete.
     */
    private void removeEvent(Event event) {
                    FirebaseManager.getInstance().deleteEvent(event.getId());
                    Toast.makeText(this, "Deleted event: " + event.getTitle(), Toast.LENGTH_SHORT).show();
                    // Refresh UI
                    loadEventsFromFirebase();
                    adapter.notifyDataSetChanged();

    }



    /**
     * Loads all events from Firestore and updates the {@link ListView}.
     * On success, refreshes the local event list and notifies the adapter.
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
                Log.d("AttendeeBrowseEvents", "Loaded " + eventList.size() + " events.");
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("AttendeeBrowseEvents", "Error loading events: " + e.getMessage());
            }
        });
    }


}