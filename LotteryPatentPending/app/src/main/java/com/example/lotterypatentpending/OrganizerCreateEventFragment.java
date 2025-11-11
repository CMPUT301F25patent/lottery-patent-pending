package com.example.lotterypatentpending;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.example.lotterypatentpending.models.Event;
import com.example.lotterypatentpending.models.FirebaseManager;
import com.example.lotterypatentpending.models.User;
import com.example.lotterypatentpending.viewModels.UserEventRepository;
import com.example.lotterypatentpending.viewModels.EventViewModel;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Fragment that allows users to create a new Event.
 * <p>
 * Collects input from the user, creates an Event object, stores it in Firestore,
 * and navigates to OrganizerEventViewFragment to display the newly created event.
 * </p>
 */
public class OrganizerCreateEventFragment extends Fragment {

    private EditText titleEt, descriptionEt, locationEt, eventDateEt, regStartDateEt, regEndDateEt, capacityEt, waitingListCapEt;
    private Button cancelBtn, createBtn;
    private FirebaseManager fm;

    /**
     * Inflates the fragment's layout.
     *
     * @param inflater The LayoutInflater object that can be used to inflate any views.
     * @param container The parent ViewGroup.
     * @param savedInstanceState Bundle containing saved instance state.
     * @return The root View of the fragment's layout.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.organizer_fragment_create_event, container, false);
    }

    /**
     * Called after the view has been created.
     * Initializes UI elements, sets click listeners for buttons.
     *
     * @param v The root view of the fragment.
     * @param savedInstanceState Bundle containing saved instance state.
     */
    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        fm = FirebaseManager.getInstance();
        titleEt = v.findViewById(R.id.titleEt);
        descriptionEt = v.findViewById(R.id.descriptionEt);
        locationEt = v.findViewById(R.id.locationEt);
        eventDateEt = v.findViewById(R.id.eventDateEt);
        regStartDateEt = v.findViewById(R.id.registrationStartDate);
        regEndDateEt = v.findViewById(R.id.registrationEndDate);
        capacityEt = v.findViewById(R.id.maxEntrantsInput);
        waitingListCapEt = v.findViewById(R.id.waitingListCapInput);
        cancelBtn = v.findViewById(R.id.cancelButton);
        createBtn = v.findViewById(R.id.createEventButton);
        cancelBtn.setOnClickListener(view ->
                NavHostFragment.findNavController(OrganizerCreateEventFragment.this)
                        .navigate(R.id.action_createEvent_to_main));
        createBtn.setOnClickListener(view -> {
            createEvent();
            NavHostFragment.findNavController(OrganizerCreateEventFragment.this)
                .navigate(R.id.action_createEvent_to_Event_View, null,
                        new NavOptions.Builder()
                        .setPopUpTo(R.id.CreateEventFragment, true)
                        .build());
            }); // removes OrganizerCreateEventFragment from stack so when back is clicked from event view doesn't go back there goes back to page beforehand
    }

    /**
     * Collects input from EditText fields, creates a new Event object,
     * saves it to Firestore, and updates the EventViewModel.
     */
    public void createEvent() {
        String title = titleEt.getText().toString().trim();
        String description = descriptionEt.getText().toString().trim();
        String location = locationEt.getText().toString().trim();
        String eventDateString = eventDateEt.getText().toString().trim();
        String regStartDateString = regStartDateEt.getText().toString().trim();
        String regEndDateString = regEndDateEt.getText().toString().trim();
        String capacityString = capacityEt.getText().toString().trim();
        String waitingListCapString = waitingListCapEt.getText().toString().trim();
        LocalDateTime eventDate = parseDate(eventDateString);
        LocalDateTime regStartDate = parseDate(regStartDateString);
        LocalDateTime regEndDate = parseDate(regEndDateString);

        int capacity = Integer.parseInt(capacityString);
        int waitingListCap = -1;
        if (!(waitingListCapString.equals("N/A")) && !(waitingListCapString.isEmpty())) {
            waitingListCap = Integer.parseInt(waitingListCapString);
        }

        // Get the current user
        User current_user = UserEventRepository.getInstance().getUser().getValue();
        assert current_user != null;

        // Create the new event
        Event newEvent = new Event(title, description, capacity, current_user);
        newEvent.setLocation(location);
        newEvent.setDate(eventDate);
        newEvent.setRegStartDate(regStartDate);
        newEvent.setRegEndDate(regEndDate);
        newEvent.setWaitingListCapacity(waitingListCap);

        // Save to Firestore
        fm.addEventToDB(newEvent);

        // Update EventViewModel for sharing with OrganizerEventViewFragment
        EventViewModel viewModel = new ViewModelProvider(requireActivity()).get(EventViewModel.class);
        viewModel.setEvent(newEvent);
    }

    private LocalDateTime parseDate(String input) {
        if (input == null || input.trim().isEmpty()) return null;

        try {
            DateTimeFormatter formatter = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a");
                return LocalDateTime.parse(input.trim(), formatter);
            }

        } catch (Exception e) {
            Log.e("CreateEvent", "Invalid date: '" + input + "'", e);
            return null;
        }

        return null;
    }

}
