package com.example.lotterypatentpending;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.util.Pair;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import com.example.lotterypatentpending.models.EventState;
import com.example.lotterypatentpending.models.FirebaseManager;
import com.example.lotterypatentpending.models.User;
import com.example.lotterypatentpending.models.WaitingList;
import com.example.lotterypatentpending.models.WaitingListState;
import com.example.lotterypatentpending.viewModels.EventViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;


/**
 * Fragment that displays the waiting list for the currently selected event.
 * Allows the organizer to view entrants, select an entrant, and cancel their
 * participation. The list is sourced from Firestore and kept in sync with the
 * shared EventViewModel.
 */
public class OrganizerViewWaitingListFragment extends Fragment {
    private MaterialButton sampleBtn;
    private Button cancelEntrantBtn;
    private Button exportBtn;
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

    private String csvContent;


    // Popup + filter state
    private PopupWindow userFilterPopup;
    private boolean filterAllUsers = true;
    private boolean filterEnteredUsers = false;
    private boolean filterSelectedUsers = false;
    private boolean filterCanceledUsers = false;

    private final ActivityResultLauncher<Intent> createCsvFileLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {

                if (result.getResultCode() == Activity.RESULT_OK) {
                    if (result.getData() != null) {
                        Uri uri = result.getData().getData();
                        writeToFile(uri, csvContent);
                    }
                }
            });

    /**
     * Inflates the waiting list layout for the organizer.
     *
     * @return The root view for this fragment.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.organizer_fragment_view_event_waiting_list, container, false);

    }
    /**
     * Initializes UI components, sets up the adapter, selection handling,
     * and loads the waiting list from Firestore.
     *
     * @param v The fragment root view.
     * @param savedInstanceState Previously saved state, if any.
     */
    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        waitinglistView = v.findViewById(R.id.waitingList);
        sampleBtn = v.findViewById(R.id.btn_lottery_sample);
        exportBtn = v.findViewById(R.id.organizer_event_waiting_list_button_export);
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
        waitingList = evm.getEvent().getValue().getWaitingList().getList();

        wLAdapter = new WaitingListAdapter(requireContext(), visibleWaitingList);
        waitinglistView.setAdapter(wLAdapter);

        // listeners
        waitinglistView.setOnItemClickListener((parent, view, position, id) -> {
            if (selectedPosition == position) {
                // Deselect
                selectedPosition = -1;
                selectedEntrant = null;
                cancelEntrantBtn.setEnabled(false);
            } else {
                // Select new entrant
                selectedPosition = position;
                selectedEntrant = visibleWaitingList.get(position);

                WaitingListState state = selectedEntrant.second;

                if (state == WaitingListState.SELECTED) {
                    // Only SELECTED (not yet accepted) can be cancelled
                    cancelEntrantBtn.setEnabled(true);
                } else {
                    cancelEntrantBtn.setEnabled(false);
                    Toast.makeText(
                            requireContext(),
                            "You can only cancel entrants who are SELECTED and have not accepted yet.",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }

            wLAdapter.setSelectedPosition(selectedPosition);
        });

        cancelEntrantBtn.setOnClickListener(v1 -> {
            if (selectedEntrant != null) {
                cancelEntrantHelper(selectedEntrant);
            } else {
                Toast.makeText(requireContext(), "Please select an entrant to cancel.", Toast.LENGTH_SHORT).show();
            }
        });

        exportBtn.setOnClickListener(v2 -> {
            exportAcceptedToCSV();
        });
        // init the button as disabled
        cancelEntrantBtn.setEnabled(false);
//        exportBtn.setEnabled(false);



        // this runs whenever the event object in the viewmodel changes
        evm.getEvent().observe(getViewLifecycleOwner(), event -> {

            if (event != null) {
                updateButtons(event);
                fetchWaitingList(event.getId());
            }
        });

    }

    private void exportAcceptedToCSV(){
        WaitingList wl = evm.getEvent().getValue().getWaitingList();

        String csv_string = wl.exportAcceptedEntrantsToCsv();
        saveCsv(csv_string, "accepted_attendees.csv");
    }

    private void saveCsv(String csvContent, String title) {
        this.csvContent = csvContent;

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.setType("text/csv");
        intent.putExtra(Intent.EXTRA_TITLE, title);
        createCsvFileLauncher.launch(intent);

    }

    private void writeToFile(Uri uri, String csv) {
        try (OutputStream os = requireContext()
                .getContentResolver()
                .openOutputStream(uri)) {

            os.write(csv.getBytes(StandardCharsets.UTF_8));
            os.flush();
            Log.d("Export To CSV", "Successfully exported attendants to csv");
            Toast.makeText(requireContext(), "CSV exported!", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Log.e("Export To CSV", "Failed to export attendants to csv", e);
            Toast.makeText(requireContext(), "Error exporting CSV", Toast.LENGTH_SHORT).show();
        }
    }


    private void fetchWaitingList(String eventId) {
        loading.show();
        fm.getEventWaitingList(eventId, new FirebaseManager.FirebaseCallback<ArrayList<Pair<User, WaitingListState>>>() {
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
    /**
     * Refreshes the ListView by notifying the adapter of data changes.
     */
    private void refreshListFromVisible(){
        wLAdapter.notifyDataSetChanged();
    }

    private void updateButtons(Event currentEvent) {
        sampleBtn.setVisibility(View.GONE);
        exportBtn.setVisibility(View.GONE);
        Log.i("OrganizerViewWaitingListFragment", "Event state: " + currentEvent.getEventState());

        EventState state = currentEvent.getEventState();

        // rule:
        // - CLOSED_FOR_REG  → you can draw winners
        // - SELECTED_ENTRANTS → you can re-draw (e.g., after declines)
        if (state == EventState.CLOSED_FOR_REG || state == EventState.SELECTED_ENTRANTS) {
            sampleBtn.setVisibility(View.VISIBLE);

            // Optional: change label depending on state
            if (state == EventState.CLOSED_FOR_REG) {
                sampleBtn.setText("Draw Attendants");
            } else {
                sampleBtn.setText("Redraw Attendants");
            }

            sampleBtn.setOnClickListener(v -> sampleBtnHelper(currentEvent));
        }

        // did a switch for all event states in case we wanted to add stuff here later as well
        switch (state) {
            case NOT_STARTED:
                break;

            case OPEN_FOR_REG:
                // nothing special for now, but kept in case we add logic later
                break;

            case SELECTED_ENTRANTS:
            case CONFIRMED_ENTRANTS:
            case CANCELLED:
            case CLOSED_FOR_REG:
            case ENDED:
                exportBtn.setVisibility(View.VISIBLE);
                exportBtn.setOnClickListener(v2 -> {
                    exportAcceptedToCSV();
                });
                break;
        }
    }

    private void sampleBtnHelper(Event event) {
        loading.show();

        // Unified lottery logic in Event
        event.runLottery();

        // Update ViewModel so the rest of the UI sees the new state
        evm.setEvent(event);

        // Push updated event (state + waiting list) to Firestore
        fm.addOrUpdateEvent(event.getId(), event);

        // Reload the waiting list so the adapter sees updated states
        fetchWaitingList(event.getId());

        if (loading != null) loading.hide();
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

        boolean useStateFilter = filterAllUsers;

        for (Pair<User, WaitingListState> entry : waitingList) {
            if (useStateFilter) {
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
