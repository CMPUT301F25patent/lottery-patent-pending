package com.example.lotterypatentpending;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.lotterypatentpending.helpers.LoadingOverlay;
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
    /**
     * Default constructor that inflates the event details layout for attendees.
     */
    public AttendeeEventDetailsFragment() {
        super(R.layout.attendee_fragment_event_details);
    }
    /**
     * Initializes UI components and wires up button actions for joining/leaving an event.
     *
     * @param view the fragment UI view
     * @param savedInstanceState previous state, if any
     */
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
    /**
     * Clears the stored event when the fragment view is destroyed
     * to prevent stale event data when navigating back.
     */
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
     *
     * @return true if the joining operation succeeded, false otherwise
     */
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

            Toast.makeText(getContext(), "Joined Event!", Toast.LENGTH_SHORT).show();

            return true;
        }
        else {
            return false;
        }
    }
    /**
     * Removes the current user from the selected event's waiting list
     * and updates local + Firestore state.
     *
     * @return true if the leave operation succeeded, false otherwise
     */
    private boolean leaveEventHelper() {
        User currentUser = userEventRepo.getUser().getValue();
        Event currentEvent = userEventRepo.getEvent().getValue();

        if (currentUser != null && currentEvent != null) {
            Log.d("DEBUG", "leaveEventHelper() event.getId() = " + currentEvent.getId());
            fm.removeEntrantFromWaitingList(currentEvent.getId(), currentUser.getUserId());
            fm.removeJoinedEventFromEntrant(currentEvent.getId(), currentUser.getUserId());

            currentEvent.removeFromWaitingList(currentUser);
            currentUser.removeJoinedEvent(currentEvent.getId());

            userEventRepo.setUser(currentUser);
            userEventRepo.setEvent(currentEvent);

            Toast.makeText(getContext(), "Left Event", Toast.LENGTH_SHORT).show();

            return true;
        }
        else {
            return false;
        }
    }
}
