package com.example.lotterypatentpending;

import android.graphics.BitmapFactory;
import android.os.Bundle;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.lotterypatentpending.data.FirestoreUsersDataSource;
import com.example.lotterypatentpending.domain.OrganizerNotifier;
import com.example.lotterypatentpending.models.Event;
import com.example.lotterypatentpending.models.FirestoreNotificationRepository;
import com.example.lotterypatentpending.domain.OrganizerNotifier;
import com.example.lotterypatentpending.models.QRGenerator;
import com.example.lotterypatentpending.models.User;
import com.example.lotterypatentpending.viewModels.EventViewModel;
import com.example.lotterypatentpending.viewModels.UserEventRepository;
import com.example.lotterypatentpending.helpers.DateTimeFormatHelper;

import android.graphics.Bitmap;


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
    private ImageView qrView, posterImage;
    private String eventId, currentOrganizerId;
    private Button viewWLBtn, viewMapBtn, generateQRCode;
    private CheckBox geoLocationReq;
    private ImageButton notiButton;
    private OrganizerNotifier organizerNotifier;
    private Event currentEvent;

    private enum TargetGroup { CHOSEN_SIGNUP, WAITLIST, ATTENDING, CANCELLED }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.organizer_fragment_event_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        EventViewModel eventViewModel = new ViewModelProvider(requireActivity()).get(EventViewModel.class);

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
            if (event == null) return;

            eventTitle.setText(event.getTitle());
            eventDescr.setText(event.getDescription());
            currentEvent = event;

            // ----- 1) Capacity usage: ACCEPTED / CAPACITY -----
            int capacity = event.getCapacity();
            int acceptedCount = 0;
            int wlCap = event.getWaitingListCapacity();
            int currentWLSize = 0;

            if (event.getWaitingList() != null &&
                    event.getWaitingList().getList() != null) {

                for (Pair<User, WaitingListState> entry : event.getWaitingList().getList()) {
                    if (entry == null) continue;

                    currentWLSize++;  // everyone in the list counts towards size

                    if (entry.second == WaitingListState.ACCEPTED) {
                        acceptedCount++;
                    }
                }
            }

            String maxEntrantsText = acceptedCount + " / " + capacity;

            // ----- 2) Waiting list usage: WL_SIZE / WL_CAP -----
            String waitListText = "";
            if (wlCap == -1) {
                // unlimited waiting list → show "size / N/A"
                waitListText += "N/A" ;
            } else {
                waitListText += currentWLSize + " / " + wlCap;
            }

            // ----- 3) Other fields (same as before) -----
            String locationText = "";
            String dateText = "";
            String regStartText = "";
            String regEndText = "";
            String tagText = "" + event.getTag();

            String location = event.getLocation();

            if (location == null || location.isEmpty()) {
                locationText += "TBD";
            } else {
                locationText += location;
            }

            if (event.getDate() == null) {
                dateText += "TBD";
            } else {
                dateText += DateTimeFormatHelper.formatTimestamp(event.getDate());
            }

            if (event.getRegStartDate() == null) {
                regStartText += "TBD";
            } else {
                regStartText += DateTimeFormatHelper.formatTimestamp(event.getRegStartDate());
            }

            if (event.getRegEndDate() == null) {
                regEndText += "TBD";
            } else {
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
            eventTitle.setText(event.getTitle());
            eventDescr.setText(event.getDescription());
            eventLocation.setText("Location: " + (event.getLocation() == null ? "TBD" : event.getLocation()));
            eventDate.setText("Date: " + (event.getDate() == null ? "TBD" : DateTimeFormatHelper.formatTimestamp(event.getDate())));
            eventRegStart.setText("Registration Start: " + (event.getRegStartDate() == null ? "TBD" : DateTimeFormatHelper.formatTimestamp(event.getRegStartDate())));
            eventRegEnd.setText("Registration End: " + (event.getRegEndDate() == null ? "TBD" : DateTimeFormatHelper.formatTimestamp(event.getRegEndDate())));
            maxEntrants.setText("Event Capacity: " + event.getCapacity());
            waitListCap.setText("Waiting List Capacity: " + (event.getWaitingListCapacity() == -1 ? "N/A" : event.getWaitingListCapacity()));
            eventTag.setText("Tag: " + event.getTag());
            geoLocationReq.setChecked(event.isGeolocationRequired());
            if (event.getPosterBytes() != null && event.getPosterBytes().length > 0) {
                posterImage.setImageBitmap(BitmapFactory.decodeByteArray(event.getPosterBytes(), 0, event.getPosterBytes().length));
            }
        });

        // --- FIX: Instantiation with 2 arguments ---
        organizerNotifier = new OrganizerNotifier(
                new FirestoreNotificationRepository(),
                new FirestoreUsersDataSource()
        );

        notiButton.setOnClickListener(view -> showNotificationOptionsDialog());
        geoLocationReq.setOnCheckedChangeListener((bv, isChecked) -> eventViewModel.updateGeoRequired(isChecked));
        generateQRCode.setOnClickListener(view -> {
            if (eventId != null) {
                QRGenerator.setQRToView(qrView, eventId, 300);
                qrView.setVisibility(View.VISIBLE);
            }
        });
        viewWLBtn.setOnClickListener(view -> NavHostFragment.findNavController(this).navigate(R.id.action_EventView_to_WaitingList));
        viewMapBtn.setOnClickListener(view -> {
            if(currentEvent != null && currentEvent.isGeolocationRequired()) {
                NavHostFragment.findNavController(this).navigate(R.id.action_EventView_to_MapView);
            } else {
                Toast.makeText(requireContext(), "Enable geo-location first.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showNotificationOptionsDialog() {
        User organizer = UserEventRepository.getInstance().getUser().getValue();
        if (organizer == null || currentEvent == null) {
            Toast.makeText(getContext(), "Data not loaded.", Toast.LENGTH_SHORT).show();
            return;
        }
        currentOrganizerId = organizer.getUserId();
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Send notification to…")
                .setItems(new CharSequence[]{
                        "Chosen entrants to sign up",  // SELECTED
                        "All waitlisted entrants",     // ENTERED
                        "All entrants attending",      // ACCEPTED
                        "All cancelled entrants"       // CANCELED
                }, (d, which) -> {
                    if (which == 0) showComposeDialog(TargetGroup.CHOSEN_SIGNUP);
                    else if (which == 1) showComposeDialog(TargetGroup.WAITLIST);
                    else if (which == 2) showComposeDialog(TargetGroup.ATTENDING);
                    else if (which == 3) showComposeDialog(TargetGroup.CANCELLED);
                }).show();
    }

    private void showComposeDialog(TargetGroup group) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_notification_compose, null);
        EditText input = dialogView.findViewById(R.id.editMessage);
        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Compose notification").setView(dialogView)
                .setNegativeButton("Cancel", null).setPositiveButton("Send", null).create();

        dialog.setOnShowListener(d -> {
            Button sendBtn = dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE);
            sendBtn.setEnabled(false);
            input.addTextChangedListener(new android.text.TextWatcher() {
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                public void onTextChanged(CharSequence s, int start, int before, int count) { sendBtn.setEnabled(s.toString().trim().length() > 0); }
                public void afterTextChanged(android.text.Editable s) {}
            });
            sendBtn.setOnClickListener(v -> {
                String body = input.getText().toString().trim();
                String eventName = currentEvent.getTitle();
                if (group == TargetGroup.CHOSEN_SIGNUP) organizerNotifier.notifySelectedToSignUp(currentOrganizerId, currentEvent.getId(), eventName, body);
                else if (group == TargetGroup.WAITLIST) organizerNotifier.notifyEntrantsInWaitlist(currentOrganizerId, currentEvent.getId(), eventName, body);
                else if (group == TargetGroup.ATTENDING) organizerNotifier.notifyEntrantsAttending(currentOrganizerId, currentEvent.getId(), eventName, body);
                else if (group == TargetGroup.CANCELLED) organizerNotifier.notifyCancelledEntrants(currentOrganizerId, currentEvent.getId(), eventName, body);

                Toast.makeText(getContext(), "Notification Sent", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });
        });
        dialog.show();
    }
}
