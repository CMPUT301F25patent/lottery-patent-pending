package com.example.lotterypatentpending;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.lotterypatentpending.helpers.DateTimeFormatHelper;
import com.example.lotterypatentpending.models.QRGenerator;
import com.example.lotterypatentpending.viewModels.EventViewModel;

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

    private TextView eventTitle, eventDescr, eventLocation, eventDate, eventRegStart, eventRegEnd, maxEntrants, waitListCap;
    private ImageView qrView;
    private String eventId;
    private Button generateQRCode;
    private CheckBox geoLocationReq;
    private ImageButton notiButton;

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
        generateQRCode = v.findViewById(R.id.qrButton);
        qrView = v.findViewById(R.id.qrImage);
        geoLocationReq = v.findViewById(R.id.geoCheck);
        notiButton = v.findViewById(R.id.notiBtn);

        EventViewModel viewModel = new ViewModelProvider(requireActivity()).get(EventViewModel.class);
        viewModel.getEvent().observe(getViewLifecycleOwner(), event -> {
            eventTitle.setText(event.getTitle());
            eventDescr.setText(event.getDescription());
            String maxEntrantsText = "Event Capacity: " + event.getCapacity();
            String locationText = "Location: ";
            String dateText = "Date: ";
            String regStartText = "Registration Start: ";
            String regEndText = "Registration End: ";
            String location = event.getLocation();
            int wlCap = event.getWaitingListCapacity();
            String waitListText = "Waiting List Capacity: ";

            if(wlCap == -1){
                waitListText += "N/A";
            }else{
                waitListText += + event.getWaitingListCapacity();
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
            eventId = event.getId();

            geoLocationReq.setChecked(event.isGeolocationRequired());

        });

        notiButton.setOnClickListener(view -> {

        });

        geoLocationReq.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.updateGeoRequired(isChecked);
        });

        generateQRCode.setOnClickListener(view ->{
            generateEventQRCode(eventId, qrView);
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

}
