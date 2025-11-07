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
import androidx.navigation.fragment.NavHostFragment;

import com.example.lotterypatentpending.models.Event;
import com.example.lotterypatentpending.models.FirebaseManager;
import com.example.lotterypatentpending.models.User;
import com.example.lotterypatentpending.viewModels.UserEventRepository;
import com.example.lotterypatentpending.viewModels.EventViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CreateEventFragment extends Fragment {

    private EditText titleEt, descriptionEt, locationEt, eventDateEt, regStartDateEt, regEndDateEt, capacityEt, waitingListCapEt;
    private Button cancelBtn, createBtn;
    private FirebaseManager fm;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_event, container, false);
    }

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
                NavHostFragment.findNavController(CreateEventFragment.this)
                        .navigate(R.id.action_createEvent_to_main));
        createBtn.setOnClickListener(view -> {
            createEvent();
            NavHostFragment.findNavController(CreateEventFragment.this)
                .navigate(R.id.action_createEvent_to_Event_View);
            });
    }

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

        User current_user = UserEventRepository.getInstance().getUser().getValue();
        assert current_user != null;
        Event newEvent = current_user.createEvent(title, description, capacity);
        newEvent.setLocation(location);
        newEvent.setDate(eventDate);
        newEvent.setRegStartDate(regStartDate);
        newEvent.setRegEndDate(regEndDate);
        newEvent.setWaitingListCapacity(waitingListCap);

        fm.addEventToDB(newEvent);

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
