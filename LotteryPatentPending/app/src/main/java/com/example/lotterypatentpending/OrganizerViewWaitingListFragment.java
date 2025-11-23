package com.example.lotterypatentpending;

import android.os.Bundle;
import androidx.core.util.Pair;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.lotterypatentpending.adapters.WaitingListAdapter;
import com.example.lotterypatentpending.helpers.LoadingOverlay;
import com.example.lotterypatentpending.models.Event;
import com.example.lotterypatentpending.models.FirebaseManager;
import com.example.lotterypatentpending.models.User;
import com.example.lotterypatentpending.models.WaitingListState;
import com.example.lotterypatentpending.viewModels.EventViewModel;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

import kotlinx.serialization.internal.ArrayClassDesc;

public class OrganizerViewWaitingListFragment extends Fragment {
    private MaterialButton sampleBtn;
    private ListView waitinglistView;
    private FirebaseManager fm;
    private LoadingOverlay loading;
    private WaitingListAdapter wLAdapter;
    private final ArrayList<Pair<User, WaitingListState>> waitingList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.organizer_fragment_view_event_waiting_list, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        waitinglistView = v.findViewById(R.id.waitingList);
        sampleBtn = v.findViewById(R.id.btn_lottery_sample);
        fm = FirebaseManager.getInstance();

        // Attach loading screen
        ViewGroup root = v.findViewById(R.id.organizer_events_root);
        View overlayView = getLayoutInflater().inflate(
                R.layout.loading_screen,
                root,
                false);

        // Add overlayView to root
        root.addView(overlayView);

        // Adds loading screen controller
        loading = new LoadingOverlay(overlayView, null);
        EventViewModel eventViewModel = new ViewModelProvider(requireActivity()).get(EventViewModel.class);
        String viewed_event_id = eventViewModel.getEvent().getValue().getId();

        wLAdapter = new WaitingListAdapter(requireContext(), waitingList);
        waitinglistView.setAdapter(wLAdapter);

        // show spinner while loading waitingList data
        loading.show();
        fm.getEventWaitingList(viewed_event_id, new FirebaseManager.FirebaseCallback<ArrayList<Pair<User, WaitingListState>>>() {
            @Override
            public void onSuccess(ArrayList<Pair<User, WaitingListState>> result) {
                waitingList.clear();
                waitingList.addAll(result);
                refreshListFromVisible();
                if (waitingList.isEmpty()){
                    Toast.makeText(requireContext(), "No entrants yet", Toast.LENGTH_SHORT).show();
                }

                if (loading != null) loading.hide();
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("FirebaseManager", "Error getting waiting list: " + e.getMessage());
                if (loading != null) loading.hide();
            }

        });

    }

    private void refreshListFromVisible(){
        wLAdapter.notifyDataSetChanged();
    }

}
