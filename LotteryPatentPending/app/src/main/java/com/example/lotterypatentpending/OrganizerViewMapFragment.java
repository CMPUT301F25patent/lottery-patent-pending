package com.example.lotterypatentpending;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.lotterypatentpending.models.FirebaseManager;
import com.example.lotterypatentpending.models.UserLocation;
import com.example.lotterypatentpending.viewModels.EventViewModel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.Firebase;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OrganizerViewMapFragment extends Fragment implements OnMapReadyCallback {
    private GoogleMap mMap;
    private FirebaseManager fm = FirebaseManager.getInstance();
    private EventViewModel viewModel;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.organizer_fragment_view_map, container, false);

        SupportMapFragment mapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);

        viewModel = new ViewModelProvider(requireActivity()).get(EventViewModel.class);

        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        ArrayList<UserLocation> entrantLocations = fm.getEntrantLocations(viewModel.getEvent().getValue().getId());
        loadUsersOnMap(entrantLocations);
    }

    private void loadUsersOnMap(ArrayList<UserLocation> userLocations) {
        if (userLocations == null || userLocations.isEmpty()) {
            return;
        }
        LatLngBounds.Builder bounds = new LatLngBounds.Builder();

        for(UserLocation location: userLocations){
            Double lat = location.getLat();
            Double lng = location.getLng();

            if (lat == null || lng == null) continue;

            LatLng userPos = new LatLng(lat, lng);

            mMap.addMarker(new MarkerOptions().position(userPos));

            // Add to bounds
            bounds.include(userPos);

        }

        // Zoom to all markers
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 100));

    }

}
