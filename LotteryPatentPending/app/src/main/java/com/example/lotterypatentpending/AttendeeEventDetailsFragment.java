package com.example.lotterypatentpending;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.lotterypatentpending.viewmodels.UserEventRepository;

public class AttendeeEventDetailsFragment extends Fragment {
    private UserEventRepository userEventRepo;

    public AttendeeEventDetailsFragment() {
        super(R.layout.fragment_attendee_event_details);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userEventRepo = UserEventRepository.getInstance();

        Button join = view.findViewById(R.id.attendee_event_details_button_join);
        join.setOnClickListener(v -> {
            userEventRepo.joinEvent();
        });
    }
}
