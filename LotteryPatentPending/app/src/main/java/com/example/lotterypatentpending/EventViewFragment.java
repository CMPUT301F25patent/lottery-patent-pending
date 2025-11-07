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
import androidx.navigation.fragment.NavHostFragment;

import com.example.lotterypatentpending.models.Event;
import com.example.lotterypatentpending.models.QRGenerator;
import com.example.lotterypatentpending.viewModels.EventViewModel;

public class EventViewFragment extends Fragment {

    private TextView eventTitle, eventDescr, maxEntrants, waitListCap;
    private ImageView qrView;
    private String eventId;
    private Button generateQRCode;
    private ImageButton backButton;
    private ImageButton homeButton;
    private CheckBox geoLocationReq;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_view, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        eventTitle = v.findViewById(R.id.eventTitle);
        eventDescr = v.findViewById(R.id.eventLongDescription);
        maxEntrants = v.findViewById(R.id.maxEntrants);
        waitListCap = v.findViewById(R.id.waitingListCap);
        generateQRCode = v.findViewById(R.id.qrButton);
        qrView = v.findViewById(R.id.qrImage);
        backButton = v.findViewById(R.id.backButton);
        homeButton = v.findViewById(R.id.homeButton);
        geoLocationReq = v.findViewById(R.id.geoCheck);

        EventViewModel viewModel = new ViewModelProvider(requireActivity()).get(EventViewModel.class);
        viewModel.getEvent().observe(getViewLifecycleOwner(), event -> {
            eventTitle.setText(event.getTitle());
            eventDescr.setText(event.getDescription());
            String maxEntrantsText = "Event Capacity: " + event.getCapacity();
            int wlCap = event.getWaitingListCapacity();
            String waitListText = "Waiting List Capacity: ";

            if(wlCap == -1){
                waitListText += "N/A";
            }else{
                waitListText += + event.getWaitingListCapacity();
            }

            maxEntrants.setText(maxEntrantsText);
            waitListCap.setText(waitListText);
            eventId = event.getId();

            geoLocationReq.setChecked(event.isGeolocationRequired());

        });

        geoLocationReq.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.updateGeoRequired(isChecked);
        });

        generateQRCode.setOnClickListener(view ->{
            generateEventQRCode(eventId, qrView);
        });

        backButton.setOnClickListener(view -> NavHostFragment.findNavController(this).popBackStack());
        homeButton.setOnClickListener(view -> requireActivity().finish());
    }

    public void generateEventQRCode(String eventId, ImageView qrView){
        QRGenerator.setQRToView(qrView, eventId, 300);
        qrView.setVisibility(View.VISIBLE);
    }

}
