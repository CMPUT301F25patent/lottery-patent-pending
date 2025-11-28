package com.example.lotterypatentpending;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.lotterypatentpending.helpers.LoadingOverlay;
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
    private LoadingOverlay loading;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.organizer_fragment_view_map, container, false);

        // Attach loading screen
        ViewGroup root = view.findViewById(R.id.organizer_events_root);
        View overlayView = getLayoutInflater().inflate(
                R.layout.loading_screen,
                root,
                false);

        // Add overlayView to root
        root.addView(overlayView);

        // Adds loading screen controller
        loading = new LoadingOverlay(overlayView, null);
        loading.show();

        SupportMapFragment mapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);

        viewModel = new ViewModelProvider(requireActivity()).get(EventViewModel.class);

        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setScrollGesturesEnabled(true);
        mMap.getUiSettings().setTiltGesturesEnabled(true);
        mMap.getUiSettings().setRotateGesturesEnabled(true);

        fm.getEntrantLocations(viewModel.getEvent().getValue().getId(), new FirebaseManager.FirebaseCallback<ArrayList<UserLocation>>() {
            @Override
            public void onSuccess(ArrayList<UserLocation> result) {
                loadUsersOnMap(result);
                loading.hide();
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("Firebase", "Failed to get entrant locations", e);
                loading.hide();
            }
        });

    }

    private void loadUsersOnMap(ArrayList<UserLocation> userLocations) {
        if (userLocations == null || userLocations.isEmpty()) {
            // No entrants → load default location
            LatLng defaultLocation = new LatLng(43.6532, -79.3832); // Toronto example
            Toast.makeText(requireContext(), "No entrants yet", Toast.LENGTH_SHORT).show();
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 10f));
            return;
        }

        LatLngBounds.Builder bounds = new LatLngBounds.Builder();

        for(UserLocation location: userLocations){
            Double lat = location.getLat();
            Double lng = location.getLng();

            Log.d("Location", "Lat: "+lat+", Lng: "+lng);

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
