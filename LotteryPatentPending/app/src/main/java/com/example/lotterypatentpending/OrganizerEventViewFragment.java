package com.example.lotterypatentpending;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lotterypatentpending.data.FirestoreUsersDataSource;
import com.example.lotterypatentpending.data.UserDataSource;
import com.example.lotterypatentpending.domain.OrganizerNotifier;
import com.example.lotterypatentpending.models.AdminLogRepository;
import com.example.lotterypatentpending.models.Event;
import com.example.lotterypatentpending.models.FirebaseManager;
import com.example.lotterypatentpending.models.FirestoreAdminLogRepository;
import com.example.lotterypatentpending.models.FirestoreNotificationRepository;
import com.example.lotterypatentpending.models.LotterySystem;
import com.example.lotterypatentpending.models.NotificationRepository;
import com.example.lotterypatentpending.models.WaitingListState;
import com.example.lotterypatentpending.viewModels.OrganizerViewModel;
import com.example.lotterypatentpending.viewModels.UserEventRepository;
import com.example.lotterypatentpending.models.User;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.lotterypatentpending.helpers.DateTimeFormatHelper;
import com.example.lotterypatentpending.models.QRGenerator;
import com.example.lotterypatentpending.viewModels.EventViewModel;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.util.List;


/**
 * Fragment that displays the details of a selected Event.
 * <p>
 * Observes an Event object from EventViewModel and updates the UI accordingly.
 * Provides functionality for generating QR codes, toggling geolocation requirement,
 * and navigating back or home.
 * </p>
 *
 * @author
 * @contributor
 */
public class OrganizerEventViewFragment extends Fragment {

    private TextView eventTitle, eventDescr, eventLocation, eventDate, eventRegStart, eventRegEnd, maxEntrants, waitListCap, eventTag;
    private ImageView qrView;

    private ImageView posterImage;

    private String eventId;
    private Button viewWLBtn, viewMapBtn, generateQRCode, runLotteryBtn;
    private CheckBox geoLocationReq;
    private ImageButton notiButton;
    private OrganizerNotifier organizerNotifier;
    private OrganizerViewModel organizerViewModel;
    private Event currentEvent;
    private String currentOrganizerId;

    // Who the notification is going to
    private enum TargetGroup {
        CHOSEN_SIGNUP,
        WAITLIST,
        SELECTED,
        CANCELLED
    }


    /**
     * Inflates the fragment's layout.
     *
     * @param inflater The LayoutInflater object to inflate views.
     * @param container The parent ViewGroup.
     * @param savedInstanceState Bundle containing saved instance state.
     * @return The root View of the fragment's layout.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.organizer_fragment_event_view, container, false);

    }

    /**
     * Called after the view has been created.
     * Initializes UI elements, sets observers and click listeners for buttons and checkbox.
     *
     * @param v The root view of the fragment.
     * @param savedInstanceState Bundle containing saved instance state.
     */
    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        eventTitle = v.findViewById(R.id.eventTitle);
        eventDescr = v.findViewById(R.id.eventLongDescription);
        eventLocation = v.findViewById(R.id.location);
        eventDate = v.findViewById(R.id.eventDate);
        eventRegStart = v.findViewById(R.id.regStart);
        eventRegEnd = v.findViewById(R.id.regEnd);
        maxEntrants = v.findViewById(R.id.maxEntrants);
        waitListCap = v.findViewById(R.id.waitingListCap);
        eventTag = v.findViewById(R.id.tag);
        generateQRCode = v.findViewById(R.id.qrButton);
        qrView = v.findViewById(R.id.qrImage);
        geoLocationReq = v.findViewById(R.id.geoCheck);
        notiButton = v.findViewById(R.id.notiBtn);
        viewWLBtn = v.findViewById(R.id.viewWLBtn);
        viewMapBtn = v.findViewById(R.id.viewMapBtn);
        posterImage = v.findViewById(R.id.eventImage);


        EventViewModel viewModel = new ViewModelProvider(requireActivity()).get(EventViewModel.class);
        viewModel.getEvent().observe(getViewLifecycleOwner(), event -> {
            eventTitle.setText(event.getTitle());
            eventDescr.setText(event.getDescription());
            currentEvent = event;
            String maxEntrantsText = "Event Capacity: " + event.getCapacity();
            String locationText = "Location: ";
            String dateText = "Date: ";
            String regStartText = "Registration Start: ";
            String regEndText = "Registration End: ";
            String location = event.getLocation();
            int wlCap = event.getWaitingListCapacity();
            String waitListText = "Waiting List Capacity: ";
            String tagText = "Tag: " + event.getTag();
            // Load event poster from Storage (if it exists)



            if(wlCap == -1){
                waitListText += "N/A";
            }else{
                waitListText += event.getWaitingListCapacity();
            }

            if(location == null || location.isEmpty()){
                locationText += "TBD";
            }else{
                locationText += location;
            }

            if(event.getDate() == null){
                dateText += "TBD";
            }else{
                dateText += DateTimeFormatHelper.formatTimestamp(event.getDate());
            }

            if(event.getRegStartDate() == null){
                regStartText += "TBD";
            }else{
                regStartText += DateTimeFormatHelper.formatTimestamp(event.getRegStartDate());
            }

            if(event.getRegEndDate() == null){
                regEndText += "TBD";
            }else{
                regEndText += DateTimeFormatHelper.formatTimestamp(event.getRegEndDate());
            }

            eventLocation.setText(locationText);
            eventDate.setText(dateText);
            eventRegStart.setText(regStartText);
            eventRegEnd.setText(regEndText);
            maxEntrants.setText(maxEntrantsText);
            waitListCap.setText(waitListText);
            eventTag.setText(tagText);
            eventId = event.getId();

            geoLocationReq.setChecked(event.isGeolocationRequired());

            byte[] posterBytes = event.getPosterBytes();
            if (posterBytes != null && posterBytes.length > 0) {
                Bitmap bmp = BitmapFactory.decodeByteArray(posterBytes, 0, posterBytes.length);
                posterImage.setImageBitmap(bmp);
            }

        });

        NotificationRepository notifRepo = new FirestoreNotificationRepository();
        UserDataSource usersDs = new FirestoreUsersDataSource();
        AdminLogRepository logRepo = new FirestoreAdminLogRepository();
        organizerNotifier = new OrganizerNotifier(notifRepo, usersDs, logRepo);

        notiButton.setOnClickListener(view -> showNotificationOptionsDialog());

        geoLocationReq.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.updateGeoRequired(isChecked);
        });

        generateQRCode.setOnClickListener(view ->{
            generateEventQRCode(eventId, qrView);
        });

        viewWLBtn.setOnClickListener(view -> {
            NavHostFragment.findNavController(OrganizerEventViewFragment.this)
                    .navigate(R.id.action_EventView_to_WaitingList);
        });

        viewMapBtn.setOnClickListener(view -> {
            Event currentEvent = viewModel.getEvent().getValue();

            if(currentEvent.isGeolocationRequired()) {
                NavHostFragment.findNavController(OrganizerEventViewFragment.this)
                        .navigate(R.id.action_EventView_to_MapView);
            }else{
                Toast.makeText(requireContext(),
                        "Enable geo-location to view entrant locations.",
                        Toast.LENGTH_LONG).show();
            }
        });

    }

    /**
     * Generates a QR code for the given event ID and sets it to the provided ImageView.
     *
     * @param eventId The ID of the event for which the QR code is generated.
     * @param qrView The ImageView to display the generated QR code.
     */
    public void generateEventQRCode(String eventId, ImageView qrView){
        QRGenerator.setQRToView(qrView, eventId, 300);
        qrView.setVisibility(View.VISIBLE);
    }

    /** First dialog: choose which group you want to message. */
    private void showNotificationOptionsDialog() {
        // Get the logged-in organizer from the shared repository
        User organizer = UserEventRepository.getInstance().getUser().getValue();

        if (organizer == null) {
            Log.e("Organizer", "Organizer user is null in UserEventRepository");
            Toast.makeText(
                    getContext(),
                    "User not loaded yet. Please try again.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if (currentEvent == null) {
            Toast.makeText(
                    getContext(),
                    "Event not loaded yet. Please try again.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        currentOrganizerId = organizer.getUserId();

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Send notification to…")
                .setItems(new CharSequence[]{
                        "Chosen entrants to sign up",
                        "All waitlisted entrants",
                        "All selected entrants",
                        "All cancelled entrants"
                }, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            // NOTE: this still needs chosenIds from UI later
                            showComposeDialog(TargetGroup.CHOSEN_SIGNUP);
                            break;
                        case 1:
                            showComposeDialog(TargetGroup.WAITLIST);
                            break;
                        case 2:
                            showComposeDialog(TargetGroup.SELECTED);
                            break;
                        case 3:
                            showComposeDialog(TargetGroup.CANCELLED);
                            break;
                    }
                })
                .show();
    }

    /**
     * Second dialog: text box to compose the actual message.
     *  @param group The recipient group chosen in the previous dialog.
     */
    private void showComposeDialog(TargetGroup group) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.dialog_notification_compose, null);
        EditText input = dialogView.findViewById(R.id.editMessage);

        input.setSelection(input.getText().length()); // cursor at end

        androidx.appcompat.app.AlertDialog dialog =
                new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                        .setTitle("Compose notification")
                        .setView(dialogView)
                        .setNegativeButton("Cancel", null)
                        // We’ll wire the positive button manually after show()
                        .setPositiveButton("Send", null)
                        .create();

        dialog.setOnShowListener(d -> {
            // Grab the real button after the dialog is shown
            final android.widget.Button sendButton =
                    dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE);

            // Initial enabled state (false if empty, true if there’s starter text)
            sendButton.setEnabled(input.getText().toString().trim().length() > 0);

            // Watch text changes to enable/disable Send
            input.addTextChangedListener(new android.text.TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    sendButton.setEnabled(s.toString().trim().length() > 0);
                }
                @Override public void afterTextChanged(android.text.Editable s) {}
            });

            // Only send when there is text (button will be disabled otherwise)
            sendButton.setOnClickListener(v -> {
                String body = input.getText().toString().trim();
                if (body.isEmpty()) {
                    // Should never hit this because button is disabled, but just in case
                    return;
                }

                switch (group) {
                    case CHOSEN_SIGNUP:
                        sendChosenSignUp(body);
                        break;
                    case WAITLIST:
                        sendWaitlist(body);
                        break;
                    case SELECTED:
                        sendSelected(body);
                        break;
                    case CANCELLED:
                        sendCancelled(body);
                        break;
                }
                dialog.dismiss();
            });
        });

        dialog.show();
    }

    private String currentOrganizerId() {
        return currentOrganizerId;
    }
    /**
     * Verifies that the current event and organizer ID have been loaded.
     * Shows a toast message on failure.
     *
     * @return true if both are loaded; false otherwise.
     */
    private boolean ensureEventAndOrganizerLoaded() {
        if (currentEvent == null) {
            Toast.makeText(getContext(), "Event not loaded yet. Please try again.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (currentOrganizerId == null || currentOrganizerId.isEmpty()) {
            Toast.makeText(getContext(), "Organizer not loaded yet. Please try again.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /**
     * Sends a notification to entrants chosen to sign up. This currently
     * uses an empty placeholder list of entrant IDs until the UI selection
     * is wired in.
     *
     */
    /** Chosen entrants who still need to sign up */
    private void sendChosenSignUp(@NonNull String body) {
        if (currentEvent == null || currentOrganizerId == null) return;

        String orgId   = currentOrganizerId;
        String eventId = currentEvent.getId();
        String title   = "Sign up for " + currentEvent.getTitle();

        // TODO: fill this from your UI when you add a multi-select
        java.util.List<String> chosenIds = new java.util.ArrayList<>();

        organizerNotifier.notifyChosenToSignup(orgId, eventId, title, body)
                .thenAccept(ids -> requireActivity().runOnUiThread(() ->
                        android.widget.Toast.makeText(
                                getContext(),
                                "Notified " + ids.size() + " chosen entrants",
                                android.widget.Toast.LENGTH_SHORT
                        ).show()
                ))
                .exceptionally(e -> {
                    e.printStackTrace();
                    requireActivity().runOnUiThread(() ->
                            android.widget.Toast.makeText(
                                    getContext(),
                                    "Failed to send notification",
                                    android.widget.Toast.LENGTH_SHORT
                            ).show());
                    return null;
                });
    }

    /** Everyone currently in the waiting list (ENTERED / NOT_SELECTED) */
    private void sendWaitlist(@NonNull String body) {
        if (currentEvent == null || currentOrganizerId == null) return;

        String orgId   = currentOrganizerId;
        String eventId = currentEvent.getId();
        String title   = "Update for " + currentEvent.getTitle();

        organizerNotifier.notifyAllWaitlist(orgId, eventId, title, body)
                .thenAccept(ids -> requireActivity().runOnUiThread(() ->
                        android.widget.Toast.makeText(
                                getContext(),
                                "Notified " + ids.size() + " waitlisted entrants",
                                android.widget.Toast.LENGTH_SHORT
                        ).show()
                ))
                .exceptionally(e -> {
                    e.printStackTrace();
                    requireActivity().runOnUiThread(() ->
                            android.widget.Toast.makeText(
                                    getContext(),
                                    "Failed to send notification",
                                    android.widget.Toast.LENGTH_SHORT
                            ).show());
                    return null;
                });
    }

    /** Everyone whose state is SELECTED / ACCEPTED (your team can tweak) */
    private void sendSelected(@NonNull String body) {
        if (currentEvent == null || currentOrganizerId == null) return;

        String orgId   = currentOrganizerId;
        String eventId = currentEvent.getId();
        String title   = "Update for " + currentEvent.getTitle();

        organizerNotifier.notifyAllSelected(orgId, eventId, title, body)
                .thenAccept(ids -> requireActivity().runOnUiThread(() ->
                        android.widget.Toast.makeText(
                                getContext(),
                                "Notified " + ids.size() + " selected entrants",
                                android.widget.Toast.LENGTH_SHORT
                        ).show()
                ))
                .exceptionally(e -> {
                    e.printStackTrace();
                    requireActivity().runOnUiThread(() ->
                            android.widget.Toast.makeText(
                                    getContext(),
                                    "Failed to send notification",
                                    android.widget.Toast.LENGTH_SHORT
                            ).show());
                    return null;
                });
    }

    /** Everyone whose state is CANCELED */
    private void sendCancelled(@NonNull String body) {
        if (currentEvent == null || currentOrganizerId == null) return;

        String orgId   = currentOrganizerId;
        String eventId = currentEvent.getId();
        String title   = "Event update: " + currentEvent.getTitle();

        organizerNotifier.notifyAllCancelled(orgId, eventId, title, body)
                .thenAccept(ids -> requireActivity().runOnUiThread(() ->
                        android.widget.Toast.makeText(
                                getContext(),
                                "Notified " + ids.size() + " cancelled entrants",
                                android.widget.Toast.LENGTH_SHORT
                        ).show()
                ))
                .exceptionally(e -> {
                    e.printStackTrace();
                    requireActivity().runOnUiThread(() ->
                            android.widget.Toast.makeText(
                                    getContext(),
                                    "Failed to send notification",
                                    android.widget.Toast.LENGTH_SHORT
                            ).show());
                    return null;
                });
    }

    private void onRunLotteryClicked() {
        if (currentEvent == null) {
            Toast.makeText(getContext(), "Event not loaded yet.", Toast.LENGTH_SHORT).show();
            return;
        }

        User organizer = UserEventRepository.getInstance().getUser().getValue();
        if (organizer == null || organizer.getUserId() == null) {
            Toast.makeText(getContext(), "Organizer not loaded.", Toast.LENGTH_SHORT).show();
            return;
        }

        String organizerId = organizer.getUserId();
        String eventId = currentEvent.getId();
        String eventTitle = currentEvent.getTitle();

        // confirm with the organizer
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Run lottery?")
                .setMessage("This will randomly select entrants from the waiting list " +
                        "for \"" + eventTitle + "\" and notify them of the result.")
                .setPositiveButton("Run", (d, which) -> runLotteryForEvent(organizerId, eventId, eventTitle))
                .setNegativeButton("Cancel", null)
                .show();
    }
    private void runLotteryForEvent(String organizerId,
                                    String eventId,
                                    String eventTitle) {

        runLotteryBtn.setEnabled(false);

        FirebaseManager fm = FirebaseManager.getInstance();

        fm.getWaitingListPairs(eventId, new FirebaseManager.FirebaseCallback<List<Pair<User, WaitingListState>>>() {
            @Override
            public void onSuccess(List<androidx.core.util.Pair<User, WaitingListState>> pairs) {
                if (pairs == null || pairs.isEmpty()) {
                    runLotteryBtn.setEnabled(true);
                    Toast.makeText(getContext(),
                            "No entrants in the waiting list.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                // 1) Run the lottery: mark SELECTED / NOT_SELECTED
                int capacity = currentEvent.getCapacity();
                // You might want: capacity - alreadyEnrolledCount if that's tracked elsewhere.
                LotterySystem.lotterySelect(pairs, capacity);

                // 2) Persist new states to Firestore
                FirebaseManager.getInstance().updateWaitingListStates(
                        eventId,
                        pairs,
                        new FirebaseManager.FirebaseCallback<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                // 3) Build allEntrantIds + winnerIds for notifications
                                java.util.List<String> allEntrantIds = new java.util.ArrayList<>();
                                java.util.List<String> winnerIds = new java.util.ArrayList<>();

                                for (androidx.core.util.Pair<User, WaitingListState> p : pairs) {
                                    User u = p.first;
                                    WaitingListState state = p.second;
                                    if (u == null || u.getUserId() == null) continue;

                                    String uid = u.getUserId();
                                    allEntrantIds.add(uid);
                                    if (state == WaitingListState.SELECTED) {
                                        winnerIds.add(uid);
                                    }
                                }

                                // 4) Fire win + lose notifications via ViewModel
                                organizerViewModel.publishResults(
                                                organizerId,
                                                eventId,
                                                eventTitle,
                                                allEntrantIds,
                                                winnerIds
                                        )
                                        .addOnSuccessListener(tasks -> {
                                            runLotteryBtn.setEnabled(true);
                                            Toast.makeText(
                                                    getContext(),
                                                    "Lottery results published and entrants notified.",
                                                    Toast.LENGTH_SHORT
                                            ).show();
                                        })
                                        .addOnFailureListener(e -> {
                                            runLotteryBtn.setEnabled(true);
                                            e.printStackTrace();
                                            Toast.makeText(
                                                    getContext(),
                                                    "Lottery ran but notifications failed.",
                                                    Toast.LENGTH_LONG
                                            ).show();
                                        });
                            }

                            @Override
                            public void onFailure(Exception e) {
                                runLotteryBtn.setEnabled(true);
                                e.printStackTrace();
                                Toast.makeText(
                                        getContext(),
                                        "Failed to update waiting list states.",
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        });
            }

            @Override
            public void onFailure(Exception e) {
                runLotteryBtn.setEnabled(true);
                e.printStackTrace();
                Toast.makeText(
                        getContext(),
                        "Failed to load waiting list.",
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }
}
