package com.example.lotterypatentpending;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.lotterypatentpending.models.Event;
import com.example.lotterypatentpending.models.User;
import com.example.lotterypatentpending.models.UserEventRepository;
import com.google.firebase.auth.FirebaseAuth;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CreateEventFragment extends Fragment {

    private EditText titleEt, descriptionEt, eventDateEt, regStartDateEt, regEndDateEt, capacityEt, waitingListCapEt;
    private Button cancelBtn, createBtn;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_event, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        titleEt = v.findViewById(R.id.titleEt);
        descriptionEt = v.findViewById(R.id.descriptionEt);
        eventDateEt = v.findViewById(R.id.eventDateEt);
        regStartDateEt = v.findViewById(R.id.registrationStartDate);
        regEndDateEt = v.findViewById(R.id.registrationEndDate);
        capacityEt = v.findViewById(R.id.maxEntrantsInput);
        waitingListCapEt = v.findViewById(R.id.waitingListCapInput);
        cancelBtn = v.findViewById(R.id.cancelButton);
        createBtn = v.findViewById(R.id.createEventButton);
        cancelBtn.setOnClickListener(view -> requireActivity().getSupportFragmentManager().popBackStack());
        createBtn.setOnClickListener(view -> createEvent());
    }

    public void createEvent(){
        String title  = titleEt.getText().toString().trim();
        String description = descriptionEt.getText().toString().trim();
        String eventDateString = eventDateEt.getText().toString().trim();
        String regStartDateString = regStartDateEt.getText().toString().trim();
        String regEndDateString = regEndDateEt.getText().toString().trim();
        String capacityString = capacityEt.getText().toString().trim();
        String waitingListCapString = waitingListCapEt.getText().toString().trim();
        LocalDateTime eventDate = null;
        LocalDateTime regStartDate = null;
        LocalDateTime regEndDate = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            eventDate = LocalDateTime.parse(eventDateString, DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a"));
            regStartDate = LocalDateTime.parse(regStartDateString, DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a"));
            regEndDate = LocalDateTime.parse(regEndDateString, DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a"));
        }
        int capacity = Integer.parseInt(capacityString);
        int waitingListCap = -1;
        if(waitingListCapString != "N/A" && waitingListCapString != null){
            waitingListCap = Integer.parseInt(waitingListCapString);
        }

        User current_user = UserEventRepository.getInstance().getUser().getValue();
        assert current_user != null;
        Event newEvent = current_user.createEvent(title, description, capacity);
        newEvent.setDate(eventDate);
        newEvent.setRegStartDate(regStartDate);
        newEvent.setRegEndDate(regEndDate);
        newEvent.setWaitingListCapacity(waitingListCap);
    }
}
