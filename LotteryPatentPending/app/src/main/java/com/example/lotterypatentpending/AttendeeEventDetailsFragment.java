package com.example.lotterypatentpending;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
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
    private TextView waitListCap;
    private Button joinButton;
    private Button acceptButton;
    private Button declineButton;
    private Button rejoinButton;
    private Button cancelButton;
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

        TextView title       = view.findViewById(R.id.eventTitle);
        TextView description = view.findViewById(R.id.eventLongDescription);
        TextView location    = view.findViewById(R.id.location);
        TextView date        = view.findViewById(R.id.eventDate);
        TextView regStart    = view.findViewById(R.id.regStart);
        TextView regEnd      = view.findViewById(R.id.regEnd);
        TextView capacity    = view.findViewById(R.id.maxEntrants);
        waitListCap = view.findViewById(R.id.waitingListCap);
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



        // Tag: just the tag value
        String tagValue = currentEvent.getTag() == null
                ? "General"
                : currentEvent.getTag();

        //  Push formatted values into TextViews
        location.setText(locationValue);
        date.setText(dateValue);
        regStart.setText(regStartValue);
        regEnd.setText(regEndValue);
        capacity.setText(capacityValue);
        // Waiting list: -1 means N/A
        refreshWaitingListUI(currentEvent, currentUser);
        tag.setText(tagValue);

        //  Join/Leave button state
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

        if (currentUser == null || currentEvent == null || getContext() == null) {
            return false;
        }


        // 1) Check if already joined
        if (isUserJoined(currentUser, currentEvent)) {
            Toast.makeText(getContext(),
                    "You are already on the waiting list for this event.",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        // 2) Enforce waiting list capacity
        int wlCap = currentEvent.getWaitingListCapacity();
        if (wlCap != -1) { // -1 means "no limit"
            int currentSize = getCurrentWaitingListSize(currentEvent);
            if (currentSize >= wlCap) {
                Toast.makeText(getContext(),
                        "Waiting list is full.",
                        Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        // 3) Firestore writes
        fm.addJoinedEventToEntrant(currentEvent, currentUser.getUserId());
        fm.addEntrantToWaitingList(currentUser, WaitingListState.ENTERED, currentEvent.getId());

        //  4) Local model updates
        currentEvent.addToWaitingList(currentUser);
        currentUser.addJoinedEvent(currentEvent.getId());

        userEventRepo.setEvent(currentEvent);

        // Waiting list: -1 means N/A
        refreshWaitingListUI(currentEvent, currentUser);

        Toast.makeText(getContext(),
                "Joined event waiting list.",
                Toast.LENGTH_SHORT).show();

        return true;
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

            refreshWaitingListUI(currentEvent, currentUser);

            userEventRepo.setUser(currentUser);
            userEventRepo.setEvent(currentEvent);

            Toast.makeText(getContext(),
                    "Left event waiting list.",
                    Toast.LENGTH_SHORT).show();

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

    private WaitingListState getUserState(@Nullable User user, @Nullable Event event) {
        if (user == null || event == null) {
            return WaitingListState.NOT_IN;
        }
        if (user.getJoinedEventIds() == null || user.getAcceptedEventIds() == null || user.getDeclinedEventIds() == null) {
            return WaitingListState.NOT_IN;
        }
        for (Pair<User, WaitingListState> u: event.getWaitingList().getList()) {
            if (user.getUserId().equals(u.first.getUserId())) {
                return u.second;
            }
        }
        return WaitingListState.NOT_IN;
    }

    /**
     * Only show one of the buttons at a time.
     */
    private void updateButtonVisibility(WaitingListState state) {
        joinButton.setVisibility(View.GONE);
        acceptButton.setVisibility(View.GONE);
        declineButton.setVisibility(View.GONE);
        rejoinButton.setVisibility(View.GONE);
        cancelButton.setVisibility(View.GONE);
        leaveButton.setVisibility(View.GONE);

        switch (state) {
            case ENTERED:
                leaveButton.setVisibility(View.VISIBLE);
                break;
            case SELECTED:
                acceptButton.setVisibility(View.VISIBLE);
                declineButton.setVisibility(View.VISIBLE);
                break;
            case NOT_SELECTED:
                break;
            case ACCEPTED:
                cancelButton.setVisibility(View.VISIBLE);
                break;
            case DECLINED:
                rejoinButton.setVisibility(View.VISIBLE);
                leaveButton.setVisibility(View.VISIBLE);
                break;
            case CANCELED:
                rejoinButton.setVisibility(View.VISIBLE);
                leaveButton.setVisibility(View.VISIBLE);
                break;
            case NOT_IN:
                throw new RuntimeException("ERROR: User not in list!");
        }
    }

    private int getCurrentWaitingListSize(@NonNull Event event) {
        if (event.getWaitingList() == null ||
                event.getWaitingList().getList() == null) {
            return 0;
        }
        return event.getWaitingList().getList().size();
    }

    private void refreshWaitingListUI(@NonNull Event event, @Nullable User user) {
        // 1) Compute sizes
        int wlCap = event.getWaitingListCapacity();
        int currentSize = getCurrentWaitingListSize(event);

        // 2) Update "X / Y" (or N/A)
        String waitListValue;
        if (wlCap == -1) {
            waitListValue = "N/A";
        } else {
            waitListValue = currentSize + " / " + wlCap;
        }
        waitListCap.setText(waitListValue);

        // 3) Show correct button (Join vs Leave)
        boolean isJoined = isUserJoined(user, event);
        updateButtonVisibility(isJoined);

        // 4) If not joined and full -> disable Join
        if (!isJoined && wlCap != -1 && currentSize >= wlCap) {
            joinButton.setEnabled(false);
            joinButton.setAlpha(0.5f);
            joinButton.setText("Waiting list full");
        } else {
            // reset Join button to normal state
            joinButton.setEnabled(true);
            joinButton.setAlpha(1f);
            joinButton.setText("Join");
        }
    }
}
