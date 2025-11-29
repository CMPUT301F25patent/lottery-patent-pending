package com.example.lotterypatentpending;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import android.widget.PopupWindow;
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
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.List;

import kotlinx.serialization.internal.ArrayClassDesc;

public class OrganizerViewWaitingListFragment extends Fragment {
    private MaterialButton sampleBtn;
    private Button cancelEntrantBtn;
    private EventViewModel evm;
    private FirebaseManager fm;
    private LoadingOverlay loading;
    private ArrayList<Pair<User, WaitingListState>> waitingList = new ArrayList<>();
    // Filtered list actually shown in ListView
    private ArrayList<Pair<User, WaitingListState>> visibleWaitingList = new ArrayList<>();

    private ListView waitinglistView;
    private WaitingListAdapter wLAdapter;
    private int selectedPosition = -1;
    private Pair<User, WaitingListState> selectedEntrant = null;

    // Popup + filter state
    private PopupWindow userFilterPopup;
    private boolean filterAllUsers = true;
    private boolean filterEnteredUsers = false;
    private boolean filterSelectedUsers = false;
    private boolean filterCanceledUsers = false;


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

        //set the filter
        View filterIcon = v.findViewById(R.id.filterDropDown);
        if (filterIcon != null) {
            filterIcon.setOnClickListener(view -> showUserFilterPopup(view));
        }

        // Adds loading screen controller
        loading = new LoadingOverlay(overlayView, null);
        evm = new ViewModelProvider(requireActivity()).get(EventViewModel.class);
        String viewed_event_id = evm.getEvent().getValue().getId();

        waitingList = evm.getEvent().getValue().getWaitingList().getList();
        wLAdapter = new WaitingListAdapter(requireContext(), visibleWaitingList);
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
                selectedEntrant = visibleWaitingList.get(position);
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
                if (result != null) {
                    waitingList.addAll(result);
                }

                applyUserFilter();  // build visibleWaitingList from waitingList

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
            Pair<User, WaitingListState> updatedEntrant =
                    new Pair<>(userToCancel, WaitingListState.CANCELED);
            waitingList.set(index, updatedEntrant);

            selectedPosition = -1;
            selectedEntrant = null;
            cancelEntrantBtn.setEnabled(false);
            wLAdapter.setSelectedPosition(selectedPosition);

            Event currentEvent = evm.getEvent().getValue();
            currentEvent.getWaitingList().setList(this.waitingList);
            evm.setEvent(currentEvent);

            fm.updateEntrantState(eventId, userToCancel.getUserId(), WaitingListState.CANCELED);

            // Rebuild visible list based on current filters
            applyUserFilter();

            // hide loading after we're done updating UI
            if (loading != null) loading.hide();

        } else {
            Toast.makeText(requireContext(), "Entrant not found in list.", Toast.LENGTH_SHORT).show();
            if (loading != null) loading.hide();
        }
    }


    private void applyUserFilter() {
        visibleWaitingList.clear();

        boolean useStateFilter = !filterAllUsers &&
                (filterEnteredUsers || filterSelectedUsers || filterCanceledUsers);

        for (Pair<User, WaitingListState> entry : waitingList) {
            if (!useStateFilter) {
                visibleWaitingList.add(entry);
                continue;
            }

            WaitingListState state = entry.second;
            boolean include = false;

            if (filterEnteredUsers && state == WaitingListState.ENTERED)  include = true;
            if (filterSelectedUsers && state == WaitingListState.SELECTED) include = true;
            if (filterCanceledUsers && state == WaitingListState.CANCELED) include = true;

            if (include) {
                visibleWaitingList.add(entry);
            }
        }

        //  clear selection because the list changed
        selectedPosition = -1;
        selectedEntrant = null;
        cancelEntrantBtn.setEnabled(false);
        wLAdapter.setSelectedPosition(selectedPosition);  // this also calls notifyDataSetChanged()
    }

    private void showUserFilterPopup(View anchor) {
        if (userFilterPopup != null && userFilterPopup.isShowing()) {
            userFilterPopup.dismiss();
            return;
        }

        View content = LayoutInflater.from(requireContext())
                .inflate(R.layout.organizer_user_popup_filter, null, false);

        userFilterPopup = new PopupWindow(
                content,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );

        userFilterPopup.setOutsideTouchable(true);
        userFilterPopup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        SwitchMaterial swAll = content.findViewById(R.id.browseAllUsers);
        SwitchMaterial swEntered = content.findViewById(R.id.browseEnteredUsers);
        SwitchMaterial swSelected = content.findViewById(R.id.browseSelectedUsers);
        SwitchMaterial swCanceled = content.findViewById(R.id.browseCanceledUsers);

        // restore current state
        swAll.setChecked(filterAllUsers);
        swEntered.setChecked(filterEnteredUsers);
        swSelected.setChecked(filterSelectedUsers);
        swCanceled.setChecked(filterCanceledUsers);

        // guard to avoid infinite loops when we call setChecked() inside listeners
        final boolean[] updating = {false};

        // helper to enforce "at least one is ON"
        Runnable ensureAtLeastOneOn = () -> {
            if (!swAll.isChecked()
                    && !swEntered.isChecked()
                    && !swSelected.isChecked()
                    && !swCanceled.isChecked()) {
                // nothing is on → force All back on
                swAll.setChecked(true);
            }
        };

        // All: when ON, turn others OFF
        swAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (updating[0]) return;
            updating[0] = true;

            filterAllUsers = isChecked;

            if (isChecked) {
                filterEnteredUsers = false;
                filterSelectedUsers = false;
                filterCanceledUsers = false;

                swEntered.setChecked(false);
                swSelected.setChecked(false);
                swCanceled.setChecked(false);
            } else {
                // if user turns All off, make sure at least one other stays on
                ensureAtLeastOneOn.run();
            }

            updating[0] = false;
        });

        // Enrolled = ENTERED
        swEntered.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (updating[0]) return;
            updating[0] = true;

            filterEnteredUsers = isChecked;
            if (isChecked) {
                filterAllUsers = false;
                swAll.setChecked(false);
            } else {
                ensureAtLeastOneOn.run();
            }

            updating[0] = false;
        });

        // Selected = SELECTED
        swSelected.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (updating[0]) return;
            updating[0] = true;

            filterSelectedUsers = isChecked;
            if (isChecked) {
                filterAllUsers = false;
                swAll.setChecked(false);
            } else {
                ensureAtLeastOneOn.run();
            }

            updating[0] = false;
        });

        // Canceled = CANCELED
        swCanceled.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (updating[0]) return;
            updating[0] = true;

            filterCanceledUsers = isChecked;
            if (isChecked) {
                filterAllUsers = false;
                swAll.setChecked(false);
            } else {
                ensureAtLeastOneOn.run();
            }

            updating[0] = false;
        });

        userFilterPopup.setOnDismissListener(() -> {
            // sync final state into fragment fields
            filterAllUsers      = swAll.isChecked();
            filterEnteredUsers  = swEntered.isChecked();
            filterSelectedUsers = swSelected.isChecked();
            filterCanceledUsers = swCanceled.isChecked();

            // just in case, enforce the rule again
            if (!filterAllUsers
                    && !filterEnteredUsers
                    && !filterSelectedUsers
                    && !filterCanceledUsers) {
                filterAllUsers = true;
            }

            applyUserFilter();
        });

        userFilterPopup.showAsDropDown(anchor, 0, 0);
    }


}
