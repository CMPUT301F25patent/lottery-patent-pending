package com.example.lotterypatentpending;

import android.os.Bundle;
import androidx.core.util.Pair;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import com.example.lotterypatentpending.viewModels.UserEventRepository;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import kotlinx.serialization.internal.ArrayClassDesc;

public class OrganizerViewWaitingListFragment extends Fragment {
    private MaterialButton sampleBtn;
    private Button cancelEntrantBtn;
    private EventViewModel evm;
    private FirebaseManager fm;
    private LoadingOverlay loading;
    private ArrayList<Pair<User, WaitingListState>> waitingList;
    private ListView waitinglistView;
    private WaitingListAdapter wLAdapter;
    private int selectedPosition = -1;
    private Pair<User, WaitingListState> selectedEntrant = null;


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
        cancelEntrantBtn = v.findViewById(R.id.organizer_event_waiting_list_button_cancel);
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
        evm = new ViewModelProvider(requireActivity()).get(EventViewModel.class);
        String viewed_event_id = evm.getEvent().getValue().getId();

        waitingList = evm.getEvent().getValue().getWaitingList().getList();
        wLAdapter = new WaitingListAdapter(requireContext(), waitingList);
        waitinglistView.setAdapter(wLAdapter);

        // listeners
        waitinglistView.setOnItemClickListener((parent, view, position, id) -> {
            if (selectedPosition == position) {
                // deselect
                selectedPosition = -1;
                selectedEntrant = null;
            }
            else {
                // select
                selectedPosition = position;
                selectedEntrant = waitingList.get(position);
            }
            wLAdapter.setSelectedPosition(selectedPosition);
            cancelEntrantBtn.setEnabled(selectedEntrant != null);
        });
        cancelEntrantBtn.setOnClickListener(v1 -> {
            if (selectedEntrant != null) {
                cancelEntrantHelper(selectedEntrant);
            } else {
                Toast.makeText(requireContext(), "Please select an entrant to cancel.", Toast.LENGTH_SHORT).show();
            }
        });
        // init the button as disabled
        cancelEntrantBtn.setEnabled(false);

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

    /**
     * Handles the logic for cancelling a selected entrant.
     * @param entrant The User/WaitingListState pair to cancel.
     */
    private void cancelEntrantHelper(Pair<User, WaitingListState> entrant) {
        if (entrant == null) return;

        loading.show();

        User userToCancel = entrant.first;
        String eventId = evm.getEvent().getValue().getId();

        int index = waitingList.indexOf(entrant);

        if (index != -1) {
            Pair<User, WaitingListState> updatedEntrant = new Pair<>(userToCancel, WaitingListState.CANCELED);
            waitingList.set(index, updatedEntrant);

            selectedPosition = -1;
            selectedEntrant = null;
            cancelEntrantBtn.setEnabled(false);
            wLAdapter.setSelectedPosition(selectedPosition);

            Event currentEvent = evm.getEvent().getValue();
            currentEvent.getWaitingList().setList(this.waitingList);
            evm.setEvent(currentEvent);

            fm.updateEntrantState(eventId, userToCancel.getUserId(), WaitingListState.CANCELED);
        }
        else {
            Toast.makeText(requireContext(), "Entrant not found in list.", Toast.LENGTH_SHORT).show();
            if (loading != null) loading.hide();
        }


    }

}
