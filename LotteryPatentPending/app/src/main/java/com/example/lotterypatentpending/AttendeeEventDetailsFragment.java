package com.example.lotterypatentpending;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.lotterypatentpending.helpers.DateTimeFormatHelper;
import com.example.lotterypatentpending.models.Event;
import com.example.lotterypatentpending.models.FirebaseManager;
import com.example.lotterypatentpending.models.User;
import com.example.lotterypatentpending.viewModels.UserEventRepository;
import com.example.lotterypatentpending.models.WaitingListState;
import com.google.firebase.firestore.ListenerRegistration;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

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
    private ImageView posterImage;
    private TextView userStateView;

    private ListenerRegistration eventListener;

    @SuppressLint("MissingPermission")
    private final ActivityResultLauncher<String> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    getUserLocation();
                } else {
                    onLocationPermissionDenied();
                }
            });


    public AttendeeEventDetailsFragment() {
        super(R.layout.attendee_fragment_event_details);
    }
    /**
     * Initializes UI components, populates event details on screen,
     * sets up button states for the current user, and attaches a real-time
     * Firestore listener to keep the event data updated.
     *
     * @param view The rootfragment view.
     * @param savedInstanceState Previous state (unused).
     */
    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userEventRepo = UserEventRepository.getInstance();
        fm = FirebaseManager.getInstance();

        TextView title = view.findViewById(R.id.eventTitle);
        TextView description = view.findViewById(R.id.eventLongDescription);
        TextView location = view.findViewById(R.id.location);
        TextView date = view.findViewById(R.id.eventDate);
        TextView regStart = view.findViewById(R.id.regStart);
        TextView regEnd = view.findViewById(R.id.regEnd);
        TextView capacity = view.findViewById(R.id.maxEntrants);
        waitListCap = view.findViewById(R.id.waitingListCap);
        TextView tag = view.findViewById(R.id.tag);
        userStateView = view.findViewById(R.id.userState);

        joinButton  = view.findViewById(R.id.attendee_event_details_button_join);
        leaveButton = view.findViewById(R.id.attendee_event_details_button_leave);
        acceptButton = view.findViewById(R.id.attendee_event_details_button_accept);
        declineButton = view.findViewById(R.id.attendee_event_details_button_decline);
        rejoinButton = view.findViewById(R.id.attendee_event_details_button_rejoin);
        cancelButton = view.findViewById(R.id.attendee_event_details_button_cancel);


        posterImage = view.findViewById(R.id.eventImage);

        // Get current event + user from repo
        Event currentEvent = userEventRepo.getEvent().getValue();
        User currentUser   = userEventRepo.getUser().getValue();

        if (currentEvent == null) {
            Toast.makeText(requireContext(),
                    "No event loaded.",
                    Toast.LENGTH_SHORT).show();
            // You can pop back or just return and not bind anything
            return;
        }

        title.setText(currentEvent.getTitle());
        description.setText(currentEvent.getDescription());

        byte[] posterBytes = currentEvent.getPosterBytes();
        if (posterBytes != null && posterBytes.length > 0) {
            Bitmap bmp = BitmapFactory.decodeByteArray(posterBytes, 0, posterBytes.length);
            posterImage.setImageBitmap(bmp);
        }

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

        tag.setText(tagValue);

        refreshWaitingListUI(currentEvent, currentUser);

        // --- Live updates for this event ---
        String eventId = currentEvent.getId();

        eventListener = fm.getEventLive(eventId,
                new FirebaseManager.FirebaseCallback<Event>() {
                    @Override
                    public void onSuccess(Event updatedEvent) {
                        if (!isAdded()) return;

                        // Keep repo in sync with latest Event from Firestore
                        userEventRepo.setEvent(updatedEvent);

                        User user = userEventRepo.getUser().getValue();

                        // Recompute waiting list + buttons from latest data
                        refreshWaitingListUI(updatedEvent, user);
                    }

                    @Override
                    public void onFailure(Exception e) {
                         Log.e("AttendeeEventDetails", "getEventLive failed", e);
                    }
                });
    }
    /**
     * Cleans up the Firestore event listener and clears the
     * current event from the shared repository when the view is destroyed.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (eventListener != null) {
            eventListener.remove();
            eventListener = null;
        }

        if (userEventRepo.getEvent().getValue() != null) {
            userEventRepo.setEvent(null);
        }
    }

    /**
     * Adds the current user to the selected event's waiting list
     * and updates local + Firestore state.
     */
    private boolean joinEventHelper() {
        User currentUser = userEventRepo.getUser().getValue();
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
        fm.addPastEventToEntrant(currentEvent, currentUser.getUserId());
        fm.addEntrantToWaitingList(currentUser, WaitingListState.ENTERED, currentEvent.getId());

        //  4) Local model updates
        currentEvent.addToWaitingList(currentUser);
        currentUser.addJoinedEvent(currentEvent.getId());
        currentUser.addPastEvent(currentEvent.getId());

        userEventRepo.setEvent(currentEvent);

        Toast.makeText(getContext(),
                "Joined event waiting list.",
                Toast.LENGTH_SHORT).show();

        navigateBack();
        return true;
    }

    /**
     * Removes the current user from the selected event's waiting list
     * and updates local + Firestore state.
     */
    private boolean leaveEventHelper() {
        User currentUser = userEventRepo.getUser().getValue();
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

            Toast.makeText(getContext(),
                    "Left event waiting list.",
                    Toast.LENGTH_SHORT).show();

            navigateBack();
            return true;
        }
        return false;
    }
    /**
     * Handles accepting an event invitation from the waiting list.
     * Updates both local user/event models and Firestore state.
     *
     * @return true if the operation succeeded, false otherwise.
     */
    private boolean acceptEventHelper() {
        User currentUser = userEventRepo.getUser().getValue();
        Event currentEvent = userEventRepo.getEvent().getValue();

        if (currentUser != null && currentEvent != null) {
            // local
            currentEvent.updateEntrantState(currentUser, WaitingListState.ACCEPTED);

            // firestore
            fm.updateEntrantState(currentEvent.getId(), currentUser.getUserId(), WaitingListState.ACCEPTED);

            userEventRepo.setUser(currentUser);
            userEventRepo.setEvent(currentEvent);

            Toast.makeText(getContext(),
                    "Accepted event!",
                    Toast.LENGTH_SHORT).show();

            navigateBack();
            return true;
        }
        return false;
    }
    /**
     * Handles declining an event invitation.
     * Updates both local user/event models and Firestore state.
     *
     * @return true if the operation succeeded, false otherwise.
     */
    private boolean declineEventHelper() {
        User currentUser = userEventRepo.getUser().getValue();
        Event currentEvent = userEventRepo.getEvent().getValue();

        if (currentUser != null && currentEvent != null) {
            // local
            currentEvent.updateEntrantState(currentUser, WaitingListState.DECLINED);

            // firestore
            fm.updateEntrantState(currentEvent.getId(), currentUser.getUserId(), WaitingListState.DECLINED);

            userEventRepo.setUser(currentUser);
            userEventRepo.setEvent(currentEvent);

            Toast.makeText(getContext(),
                    "Declined event!",
                    Toast.LENGTH_SHORT).show();

            navigateBack();
            return true;
        }
        return false;
    }

    private boolean cancelEventHelper() {
        User currentUser = userEventRepo.getUser().getValue();
        Event currentEvent = userEventRepo.getEvent().getValue();

        if (currentUser != null && currentEvent != null) {
            // local update to waitlistState
            currentEvent.updateEntrantState(currentUser, WaitingListState.CANCELED);

            fm.updateEntrantState(currentEvent.getId(), currentUser.getUserId(), WaitingListState.CANCELED);

            userEventRepo.setUser(currentUser);
            userEventRepo.setEvent(currentEvent);

            Toast.makeText(getContext(),
                    "Canceled event!",
                    Toast.LENGTH_SHORT).show();

            navigateBack();
            return true;
        }
        return false;
    }

    private boolean rejoinEventHelper() {
        User currentUser = userEventRepo.getUser().getValue();
        Event currentEvent = userEventRepo.getEvent().getValue();

        if (currentUser != null && currentEvent != null) {
            // local
            currentEvent.updateEntrantState(currentUser, WaitingListState.NOT_SELECTED);

            fm.updateEntrantState(currentEvent.getId(), currentUser.getUserId(), WaitingListState.NOT_SELECTED);

            userEventRepo.setUser(currentUser);
            userEventRepo.setEvent(currentEvent);

            Toast.makeText(getContext(),
                    "You have rejoined the event!",
                    Toast.LENGTH_SHORT).show();

            navigateBack();
            return true;
        }

        return false;
    }

    /**
     * Returns true if the event's waiting list already contains this user
     * in any state (ENTERED, SELECTED, ACCEPTED, etc.).
     */
    private boolean isUserJoined(@Nullable User user, @Nullable Event event) {
        if (user == null || event == null) return false;
        return event.getWaitingListStateForUser(user) != WaitingListState.NOT_IN;
    }

    /**
     * Only show one of the buttons at a time.
     */
    private void updateButtons(WaitingListState state) {
        User currentUser = userEventRepo.getUser().getValue();
        Event currentEvent = userEventRepo.getEvent().getValue();

        joinButton.setVisibility(View.GONE);
        acceptButton.setVisibility(View.GONE);
        declineButton.setVisibility(View.GONE);
        rejoinButton.setVisibility(View.GONE);
        cancelButton.setVisibility(View.GONE);
        leaveButton.setVisibility(View.GONE);

        if (!currentEvent.isOpenForReg()) {
            userStateView.setText("Registration is not open/closed");
            return;
        }

        if (!currentEvent.containsUser(currentUser)) {
            joinButton.setVisibility(View.VISIBLE);
            joinButton.setOnClickListener(v -> {
                if (currentEvent.isGeolocationRequired()) {
                    requestLocationPermission();
                } else {
                    joinEventHelper();
                }
            });
            return;
        }

        switch (state) {
            case ENTERED:
                userStateView.setText("You have entered the event, you will be entered into a raffle for the event");
                leaveButton.setVisibility(View.VISIBLE);
                leaveButton.setOnClickListener(v -> {
                    leaveEventHelper();
                });
                break;

            case SELECTED:
                userStateView.setText("You have been selected for the event");
                acceptButton.setVisibility(View.VISIBLE);
                declineButton.setVisibility(View.VISIBLE);
                acceptButton.setOnClickListener(v -> {
                    acceptEventHelper();
                });
                declineButton.setOnClickListener(v -> {
                    declineEventHelper();
                });
                break;

            case NOT_SELECTED:
                userStateView.setText("Not selected for event, but if not all users accept you will be entered into raffle again");
                leaveButton.setVisibility(View.VISIBLE);
                leaveButton.setOnClickListener(v -> {
                    leaveEventHelper();
                });
                break;

            case ACCEPTED:
                userStateView.setText("Congradulations, you have been accepted to the event");
                cancelButton.setVisibility(View.VISIBLE);
                cancelButton.setOnClickListener(v -> {
                    cancelEventHelper();
                });
                break;

            case DECLINED:
                userStateView.setText("You have declinded the event invitation, you can leave the event or rejoin");

                rejoinButton.setVisibility(View.VISIBLE);
                rejoinButton.setOnClickListener(v -> {
                    rejoinEventHelper();
                });
                leaveButton.setVisibility(View.VISIBLE);
                leaveButton.setOnClickListener(v -> {
                    leaveEventHelper();
                });

                break;

            case CANCELED:
                userStateView.setText("You have canceled the event, you no longer can rejoin the event");

                leaveButton.setVisibility(View.VISIBLE);
                leaveButton.setOnClickListener(v -> {
                    leaveEventHelper();
                });
                break;

            case NOT_IN:
                throw new RuntimeException("ERROR: User not in list!");
        }
    }
    /**
     * Returns the number of entrants currently on the event's waiting list.
     *
     * @param event The event whose waiting list is being measured.
     * @return The count of entrants, or 0 if no list exists.
     */
    private int getCurrentWaitingListSize(@NonNull Event event) {
        if (event.getWaitingList() == null ||
                event.getWaitingList().getList() == null) {
            return 0;
        }
        return event.getWaitingList().getList().size();
    }
    /**
     * Updates waiting-list count text and refreshes button visibility/state
     * based on the user's current waiting-list status.
     *
     * @param event The event whose UI state is being updated.
     * @param user  The current user (may be null).
     */
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

        // 3) Show correct buttons
        WaitingListState state = event.getWaitingListStateForUser(user);
        updateButtons(state);
    }
    /**
     * Requests fine-location permission if not already granted.
     * If permission is already available, proceeds to obtain location immediately.
     */
    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED) {

            getUserLocation();

        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }
    /**
     * Handles the UI/Toast messaging for when the user denies
     * the required location permission.
     */
    private void onLocationPermissionDenied() {
        boolean shouldShowRationale =
                shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION);

        if (shouldShowRationale) {
            // User tapped "Deny" (NOT "Never ask again")
            Toast.makeText(requireContext(),
                    "Location is required to join this event. Please allow it if you wish to join.",
                    Toast.LENGTH_LONG).show();
        } else {
            // User tapped "Never Ask Again"
            // OR permanently denied in settings
            Toast.makeText(requireContext(),
                    "Location permanently denied. Enable it in settings to join this event.",
                    Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Retrieves the user's last known location (requires fine/coarse
     * location permissions) and saves it to Firestore, then attempts
     * to join the event.
     */

    @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    private void getUserLocation() {
        User currentUser = userEventRepo.getUser().getValue();

        FusedLocationProviderClient client =
                LocationServices.getFusedLocationProviderClient(requireContext());

        client.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        fm.saveLocationToFirestore(currentUser.getUserId(), location.getLatitude(), location.getLongitude());
                    }
                });

        joinEventHelper();
    }

    private void navigateBack() {
        if (!isAdded()) return;
        getParentFragmentManager().popBackStack();
    }
}

