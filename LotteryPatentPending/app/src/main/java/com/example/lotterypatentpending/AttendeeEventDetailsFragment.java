package com.example.lotterypatentpending;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.lotterypatentpending.helpers.DateTimeFormatHelper;
import com.example.lotterypatentpending.models.Event;
import com.example.lotterypatentpending.models.FirebaseManager;
import com.example.lotterypatentpending.models.User;
import com.example.lotterypatentpending.viewModels.UserEventRepository;
import com.example.lotterypatentpending.models.WaitingListState;

import java.util.Objects;

/**
 * Fragment that displays details for a selected event from the attendee view.
 * Users can join or leave the event's waiting list from this screen.
 *
 * This fragment is typically shown when an attendee taps on an event in the event list.
 */
public class AttendeeEventDetailsFragment extends Fragment {

    private UserEventRepository userEventRepo;
    private FirebaseManager fm;

    private Button joinButton;
    private Button leaveButton;

    public AttendeeEventDetailsFragment() {
        super(R.layout.attendee_fragment_event_details);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userEventRepo = UserEventRepository.getInstance();
        fm = FirebaseManager.getInstance();

        // These IDs must match your XML:
        TextView title       = view.findViewById(R.id.eventTitle);
        TextView description = view.findViewById(R.id.eventLongDescription);
        TextView location    = view.findViewById(R.id.location);
        TextView date        = view.findViewById(R.id.eventDate);
        TextView regStart    = view.findViewById(R.id.regStart);
        TextView regEnd      = view.findViewById(R.id.regEnd);
        TextView capacity    = view.findViewById(R.id.maxEntrants);
        TextView waitListCap = view.findViewById(R.id.waitingListCap);
        TextView tag         = view.findViewById(R.id.tag);

        joinButton  = view.findViewById(R.id.Join);
        leaveButton = view.findViewById(R.id.Leave);

        // Get current event + user from repo
        Event currentEvent = Objects.requireNonNull(userEventRepo.getEvent().getValue());
        User currentUser   = userEventRepo.getUser().getValue();

        title.setText(currentEvent.getTitle());
        description.setText(currentEvent.getDescription());

        // Location
        String locationValue;
        if (currentEvent.getLocation() == null ||
                currentEvent.getLocation().trim().isEmpty()) {
            locationValue = "TBD";
        } else {
            locationValue = currentEvent.getLocation();
        }

        // Date, reg start, reg end
        String dateValue;
        if (currentEvent.getDate() == null) {
            dateValue = "TBD";
        } else {
            dateValue = DateTimeFormatHelper.formatTimestamp(currentEvent.getDate());
        }

        String regStartValue;
        if (currentEvent.getRegStartDate() == null) {
            regStartValue = "TBD";
        } else {
            regStartValue = DateTimeFormatHelper.formatTimestamp(currentEvent.getRegStartDate());
        }

        String regEndValue;
        if (currentEvent.getRegEndDate() == null) {
            regEndValue = "TBD";
        } else {
            regEndValue = DateTimeFormatHelper.formatTimestamp(currentEvent.getRegEndDate());
        }

        // Capacity: just the number as text
        String capacityValue = String.valueOf(currentEvent.getCapacity());

        // Waiting list: -1 means N/A
        String waitListValue;
        int wlCap = currentEvent.getWaitingListCapacity();
        if (wlCap == -1) {
            waitListValue = "N/A";
        } else {
            waitListValue = String.valueOf(wlCap);
        }

        // Tag: just the tag value
        String tagValue = currentEvent.getTag() == null
                ? "General"
                : currentEvent.getTag();

        // ---- Push formatted values into TextViews ----
        location.setText(locationValue);
        date.setText(dateValue);
        regStart.setText(regStartValue);
        regEnd.setText(regEndValue);
        capacity.setText(capacityValue);
        waitListCap.setText(waitListValue);
        tag.setText(tagValue);

        // ---- Join/Leave button state ----
        boolean isJoined = isUserJoined(currentUser, currentEvent);
        updateButtonVisibility(isJoined);

        joinButton.setOnClickListener(v -> {
            if (joinEventHelper()) {
                updateButtonVisibility(true);
            }
        });

        leaveButton.setOnClickListener(v -> {
            if (leaveEventHelper()) {
                updateButtonVisibility(false);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (userEventRepo.getEvent().getValue() != null) {
            userEventRepo.setEvent(null);
        }
    }

    /**
     * Adds the current user to the selected event's waiting list
     * and updates local + Firestore state.
     */
    private boolean joinEventHelper() {
        User currentUser  = userEventRepo.getUser().getValue();
        Event currentEvent = userEventRepo.getEvent().getValue();

        if (currentUser != null && currentEvent != null) {
            // Firestore
            fm.addJoinedEventToEntrant(currentEvent, currentUser.getUserId());
            fm.addEntrantToWaitingList(currentUser, WaitingListState.ENTERED, currentEvent.getId());

            // Local model
            currentEvent.addToWaitingList(currentUser);
            currentUser.addJoinedEvent(currentEvent.getId());

            userEventRepo.setUser(currentUser);
            userEventRepo.setEvent(currentEvent);

            return true;
        }
        return false;
    }

    /**
     * Removes the current user from the selected event's waiting list
     * and updates local + Firestore state.
     */
    private boolean leaveEventHelper() {
        User currentUser  = userEventRepo.getUser().getValue();
        Event currentEvent = userEventRepo.getEvent().getValue();

        if (currentUser != null && currentEvent != null) {
            // Firestore
            fm.removeEntrantFromWaitingList(currentEvent.getId(), currentUser.getUserId());
            fm.removeJoinedEventFromEntrant(currentEvent.getId(), currentUser.getUserId());

            // Local model
            currentEvent.removeFromWaitingList(currentUser);
            currentUser.removeJoinedEvent(currentEvent.getId());

            userEventRepo.setUser(currentUser);
            userEventRepo.setEvent(currentEvent);

            return true;
        }
        return false;
    }

    /**
     * Uses User.joinedEventIds to decide if this user is already joined to this event.
     */
    private boolean isUserJoined(@Nullable User user, @Nullable Event event) {
        if (user == null || event == null) {
            return false;
        }
        if (user.getJoinedEventIds() == null) {
            return false;
        }
        return user.getJoinedEventIds().contains(event.getId());
    }

    /**
     * Only show one of the buttons at a time.
     */
    private void updateButtonVisibility(boolean isJoined) {
        if (isJoined) {
            joinButton.setVisibility(View.GONE);
            leaveButton.setVisibility(View.VISIBLE);
        } else {
            joinButton.setVisibility(View.VISIBLE);
            leaveButton.setVisibility(View.GONE);
        }
    }
}
