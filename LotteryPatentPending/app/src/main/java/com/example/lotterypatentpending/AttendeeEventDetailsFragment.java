package com.example.lotterypatentpending;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.lotterypatentpending.models.Event;
import com.example.lotterypatentpending.models.FirebaseManager;
import com.example.lotterypatentpending.models.User;
import com.example.lotterypatentpending.models.UserEventRepository;
import com.example.lotterypatentpending.models.WaitingListState;

import java.util.Objects;

public class AttendeeEventDetailsFragment extends Fragment {
    private UserEventRepository userEventRepo;
    private FirebaseManager fm;

    public AttendeeEventDetailsFragment() {
        super(R.layout.fragment_attendee_event_details);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userEventRepo = UserEventRepository.getInstance();
        fm = FirebaseManager.getInstance();

        TextView title = view.findViewById(R.id.attendee_event_details_textview_event_name);
        TextView description = view.findViewById(R.id.attendee_event_details_textview_description);
        Button join = view.findViewById(R.id.attendee_event_details_button_join);
        Button leave = view.findViewById(R.id.attendee_event_details_button_leave);

        title.setText(Objects.requireNonNull(userEventRepo.getEvent().getValue()).getTitle());
        description.setText(Objects.requireNonNull(userEventRepo.getEvent().getValue()).getDescription());

        // TODO: Check if user in event first, make according button visible and other invisible
        join.setOnClickListener(v -> {
            this.joinEventHelper();
        });
        leave.setOnClickListener(v ->{
            this.leaveEventHelper();
        });
    }

    private boolean joinEventHelper() {
        User currentUser = userEventRepo.getUser().getValue();
        Event currentEvent = userEventRepo.getEvent().getValue();

        if (currentUser != null && currentEvent != null) {
            fm.addJoinedEventToEntrant(currentEvent, currentUser.getUserId());
            fm.addEntrantToWaitingList(currentUser, WaitingListState.ENTERED, currentEvent.getId());

            currentEvent.addToWaitingList(currentUser);
            currentUser.addJoinedEvent(currentEvent.getId());

            userEventRepo.setUser(currentUser);
            userEventRepo.setEvent(currentEvent);

            return true;
        }
        else {
            return false;
        }
    }

    private boolean leaveEventHelper() {
        User currentUser = userEventRepo.getUser().getValue();
        Event currentEvent = userEventRepo.getEvent().getValue();

        if (currentUser != null && currentEvent != null) {
            fm.removeEntrantFromWaitingList(currentEvent.getId(), currentUser.getUserId());
            fm.removeJoinedEventFromEntrant(currentEvent.getId(), currentUser.getUserId());

            currentEvent.removeFromWaitingList(currentUser);
            currentUser.removeJoinedEvent(currentEvent.getId());

            userEventRepo.setUser(currentUser);
            userEventRepo.setEvent(currentEvent);

            return true;
        }
        else {
            return false;
        }
    }
}
