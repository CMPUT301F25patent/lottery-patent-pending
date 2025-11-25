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
import com.example.lotterypatentpending.models.NotificationRepository;
import com.example.lotterypatentpending.viewModels.UserEventRepository;
import com.example.lotterypatentpending.models.User;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.lotterypatentpending.helpers.DateTimeFormatHelper;
import com.example.lotterypatentpending.models.QRGenerator;
import com.example.lotterypatentpending.viewModels.EventViewModel;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;


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
    private Button viewWLBtn, viewMapBtn, viewAttendantsBtn, generateQRCode;
    private CheckBox geoLocationReq;
    private ImageButton notiButton;
    private OrganizerNotifier organizerNotifier;
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
        viewAttendantsBtn = v.findViewById(R.id.viewAttendantsBtn);
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
                posterImage.setVisibility(View.VISIBLE);
            } else {
                posterImage.setImageDrawable(null);
                posterImage.setVisibility(View.GONE);
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

        });

        viewAttendantsBtn.setOnClickListener(view -> {

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

    /** Second dialog: text box to compose the actual message. */
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

    /** Chosen entrants – still needs real chosenIds list wired up later. */
    private void sendChosenSignUp(String body) {
        String orgId = currentOrganizerId();
        String eventId = currentEvent.getId();
        String title = "Sign up for " + currentEvent.getTitle();

        // TODO: supply real chosenIds from UI selection
        java.util.List<String> chosenIds = new java.util.ArrayList<>();

        organizerNotifier.notifyChosenToSignup(orgId, eventId, title, body, chosenIds)
                .thenAccept(ids -> requireActivity().runOnUiThread(() ->
                        Toast.makeText(
                                getContext(),
                                "Notified " + ids.size() + " entrants",
                                Toast.LENGTH_SHORT
                        ).show()))
                .exceptionally(e -> {
                    e.printStackTrace();
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(
                                    getContext(),
                                    "Failed to send notification",
                                    Toast.LENGTH_SHORT
                            ).show());
                    return null;
                });
    }

    private void sendWaitlist(String body) {
        String orgId = currentOrganizerId();
        String eventId = currentEvent.getId();
        String title = "Update for " + currentEvent.getTitle();

        organizerNotifier.notifyAllWaitlist(orgId, eventId, title, body)
                .thenAccept(ids -> requireActivity().runOnUiThread(() ->
                        Toast.makeText(
                                getContext(),
                                "Notified " + ids.size() + " waitlisted entrants",
                                Toast.LENGTH_SHORT
                        ).show()))
                .exceptionally(e -> {
                    e.printStackTrace();
                    return null;
                });
    }

    private void sendSelected(String body) {
        String orgId = currentOrganizerId();
        String eventId = currentEvent.getId();
        String title = "Update for " + currentEvent.getTitle();

        organizerNotifier.notifyAllSelected(orgId, eventId, title, body)
                .thenAccept(ids -> requireActivity().runOnUiThread(() ->
                        Toast.makeText(
                                getContext(),
                                "Notified " + ids.size() + " selected entrants",
                                Toast.LENGTH_SHORT
                        ).show()))
                .exceptionally(e -> {
                    e.printStackTrace();
                    return null;
                });
    }

    private void sendCancelled(String body) {
        String orgId = currentOrganizerId();
        String eventId = currentEvent.getId();
        String title = "Event cancelled: " + currentEvent.getTitle();

        organizerNotifier.notifyAllCancelled(orgId, eventId, title, body)
                .thenAccept(ids -> requireActivity().runOnUiThread(() ->
                        Toast.makeText(
                                getContext(),
                                "Notified " + ids.size() + " cancelled entrants",
                                Toast.LENGTH_SHORT
                        ).show()))
                .exceptionally(e -> {
                    e.printStackTrace();
                    return null;
                });
    }
}
