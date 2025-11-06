package com.example.lotterypatentpending;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
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

public class AdminEventsActivity extends AppCompatActivity {

    private FirebaseManager firebaseManager;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<Event> eventList = new ArrayList<>();
    private List<String> eventDisplayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_events);

        listView = findViewById(R.id.eventListView);
        firebaseManager = FirebaseManager.getInstance();

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, eventDisplayList);
        listView.setAdapter(adapter);

        loadEventsFromFirebase();

        // Long press → delete event
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            Event selectedEvent = eventList.get(position);

            new AlertDialog.Builder(AdminEventsActivity.this)
                    .setTitle("Delete Event")
                    .setMessage("Are you sure you want to delete the event \"" + selectedEvent.getTitle() + "\"?")
                    .setPositiveButton("Delete", (dialog, which) -> removeEvent(selectedEvent.getId()))
                    .setNegativeButton("Cancel", null)
                    .show();

            return true;
        });
    }

    public void removeEvent(String eventId) {

        if (eventId == null || eventId.trim().isEmpty()) {
            Toast.makeText(this, "Invalid event ID.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseManager firebaseManager = FirebaseManager.getInstance();

        firebaseManager.getEvent(eventId, new FirebaseManager.FirebaseCallback<Event>() {
            @Override
            public void onSuccess(Event event) {
                if (event == null) {
                    Toast.makeText(AdminEventsActivity.this, "Event not found.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 🗑️ Confirm deletion with user
                new AlertDialog.Builder(AdminEventsActivity.this)
                        .setTitle("Delete Event")
                        .setMessage("Are you sure you want to permanently delete \""
                                + event.getTitle() + "\" organized by "
                                + (event.getOrganizer() != null ? event.getOrganizer().getName() : "Unknown") + "?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            firebaseManager.deleteEvent(event.getId());
                            Toast.makeText(AdminEventsActivity.this,
                                    "Event \"" + event.getTitle() + "\" deleted successfully.",
                                    Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                        .show();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(AdminEventsActivity.this,
                        "Error retrieving event: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
                Log.e("Admin", "Failed to get event for deletion", e);
            }
        });
    }


    private void loadEventsFromFirebase() {
        firebaseManager.getAllEvents(new FirebaseManager.FirebaseCallback<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot result) {
                eventList.clear();
                eventDisplayList.clear();

                for (DocumentSnapshot doc : result) {
                    try {
                        // Convert Firestore document to Event
                        Map<String, Object> data = doc.getData();
                        if (data == null) continue;

                        Event event = firebaseManager.mapToEvent(data);
                        if (event == null) continue;

                        eventList.add(event);

                        // Build a readable display string
                        String organizerName = (event.getOrganizer() != null)
                                ? event.getOrganizer().getName()
                                : "Unknown Organizer";

                        String display = event.getTitle() +
                                "\nBy: " + organizerName +
                                "\n" + event.getDescription() +
                                "\nLocation: " + event.getLocation() +
                                " | Capacity: " + event.getCapacity();

                        eventDisplayList.add(display);

                    } catch (Exception e) {
                        Log.e("BrowseEvents", "Error parsing event: " + e.getMessage());
                    }
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