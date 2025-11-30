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
 * Allows the organizer to view entrants and cancel their participation.
 * The list is sourced from Firestore and kept in sync with the shared
 * EventViewModel.
 */
public class OrganizerViewWaitingListFragment extends Fragment {

    private MaterialButton sampleBtn;
    private Button cancelEntrantBtn;
    private Button exportBtn;

    private EventViewModel evm;
    private FirebaseManager fm;
    private LoadingOverlay loading;

    // Full waiting list from Firestore
    private final ArrayList<Pair<User, WaitingListState>> waitingList = new ArrayList<>();
    // Filtered list actually shown in ListView
    private final ArrayList<Pair<User, WaitingListState>> visibleWaitingList = new ArrayList<>();

    private ListView waitinglistView;
    private WaitingListAdapter wLAdapter;

    private String csvContent;

    // Popup + filter state
    private PopupWindow userFilterPopup;
    private boolean filterAllUsers = true;
    private boolean filterEnteredUsers = false;
    private boolean filterSelectedUsers = false;
    private boolean filterCanceledUsers = false;

    private final ActivityResultLauncher<Intent> createCsvFileLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    writeToFile(uri, csvContent);
                }
            });

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(
                R.layout.organizer_fragment_view_event_waiting_list,
                container,
                false
        );
    }

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
                false
        );
        root.addView(overlayView);

        // Add loading screen controller
        loading = new LoadingOverlay(overlayView, null);

        // Set up filter icon
        View filterIcon = v.findViewById(R.id.filterDropDown);
        if (filterIcon != null) {
            filterIcon.setOnClickListener(view -> showUserFilterPopup(view));
        }

        // ViewModel
        evm = new ViewModelProvider(requireActivity()).get(EventViewModel.class);

        // Adapter + ListView
        wLAdapter = new WaitingListAdapter(requireContext(), visibleWaitingList);
        waitinglistView.setAdapter(wLAdapter);

        // Button handlers
        cancelEntrantBtn.setOnClickListener(v1 -> cancelAllUnacceptedEntrants());

        exportBtn.setOnClickListener(v2 -> exportAcceptedToCSV());

        // Observe event changes
        evm.getEvent().observe(getViewLifecycleOwner(), event -> {
            if (event != null) {
                updateButtons(event);
                fetchWaitingList(event.getId());
            }
        });
    }

    /** Exports accepted entrants to CSV via Storage Access Framework. */
    private void exportAcceptedToCSV() {
        Event event = evm.getEvent().getValue();
        if (event == null || event.getWaitingList() == null) {
            Toast.makeText(requireContext(),
                    "No accepted entrants to export.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        WaitingList wl = event.getWaitingList();
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

            if (os == null) {
                throw new IllegalStateException("OutputStream is null");
            }

            os.write(csv.getBytes(StandardCharsets.UTF_8));
            os.flush();
            Log.d("Export To CSV", "Successfully exported attendants to csv");
            Toast.makeText(requireContext(), "CSV exported!", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Log.e("Export To CSV", "Failed to export attendants to csv", e);
            Toast.makeText(requireContext(), "Error exporting CSV", Toast.LENGTH_SHORT).show();
        }
    }

    /** Fetches the waiting list from Firestore and updates the adapter. */
    private void fetchWaitingList(String eventId) {
        loading.show();

        fm.getEventWaitingList(eventId, new FirebaseManager.FirebaseCallback<ArrayList<Pair<User, WaitingListState>>>() {
            @Override
            public void onSuccess(ArrayList<Pair<User, WaitingListState>> result) {
                waitingList.clear();
                if (result != null) {
                    waitingList.addAll(result);
                }

                applyUserFilter();      // rebuild visibleWaitingList

                if (waitingList.isEmpty()) {
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

    /** Shows/hides buttons based on event state. */
    private void updateButtons(Event currentEvent) {
        sampleBtn.setVisibility(View.GONE);
        exportBtn.setVisibility(View.GONE);

        Log.i("OrganizerViewWaitingListFragment",
                "Event state: " + currentEvent.getEventState());

        EventState state = currentEvent.getEventState();

        // Lottery button
        if (state == EventState.CLOSED_FOR_REG || state == EventState.SELECTED_ENTRANTS) {
            sampleBtn.setVisibility(View.VISIBLE);

            if (state == EventState.CLOSED_FOR_REG) {
                sampleBtn.setText("Draw Attendants");
            } else {
                sampleBtn.setText("Redraw Attendants");
            }

            sampleBtn.setOnClickListener(v -> sampleBtnHelper(currentEvent));
        }

        // Export button
        switch (state) {
            case SELECTED_ENTRANTS:
            case CONFIRMED_ENTRANTS:
            case CANCELLED:
            case CLOSED_FOR_REG:
            case ENDED:
                exportBtn.setVisibility(View.VISIBLE);
                // listener already set in onViewCreated
                break;

            case NOT_STARTED:
            case OPEN_FOR_REG:
            default:
                // nothing special
                break;
        }
    }

    /** Runs the lottery and refreshes the waiting list. */
    private void sampleBtnHelper(Event event) {
        loading.show();

        event.runLottery();
        evm.setEvent(event);
        fm.addOrUpdateEvent(event.getId(), event);

        fetchWaitingList(event.getId());

        if (loading != null) loading.hide();
    }

    /**
     * Cancels all entrants who are still SELECTED (invited but not accepted).
     */
    /**
     * Cancels all entrants who are still SELECTED (i.e., were drawn but have not accepted).
     */
    private void cancelAllUnacceptedEntrants() {
        Event currentEvent = evm.getEvent().getValue();
        if (currentEvent == null ||
                currentEvent.getWaitingList() == null ||
                currentEvent.getWaitingList().getList() == null) {
            Toast.makeText(requireContext(),
                    "Cancel entrants is not available right now.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        loading.show();

        String eventId = currentEvent.getId();
        ArrayList<Pair<User, WaitingListState>> list = currentEvent.getWaitingList().getList();

        boolean anyToCancel = false;

        for (int i = 0; i < list.size(); i++) {
            Pair<User, WaitingListState> entry = list.get(i);
            WaitingListState state = entry.second;

            // Treat SELECTED as "invited but not yet accepted"
            if (state == WaitingListState.SELECTED) {
                anyToCancel = true;
                User user = entry.first;

                // Update local list
                list.set(i, new Pair<>(user, WaitingListState.CANCELED));

                // Update Firestore for this entrant
                fm.updateEntrantState(eventId, user.getUserId(), WaitingListState.CANCELED);
            }
        }

        if (!anyToCancel) {
            loading.hide();
            Toast.makeText(
                    requireContext(),
                    "Cancel entrants is not available right now.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        // Update fragment-level list + ViewModel
        waitingList.clear();
        waitingList.addAll(list);

        currentEvent.getWaitingList().setList(list);
        evm.setEvent(currentEvent);

        // Rebuild visible list based on current filters
        applyUserFilter();

        loading.hide();

        Toast.makeText(
                requireContext(),
                "Entrants were cancelled.",
                Toast.LENGTH_SHORT
        ).show();
    }

    /** Applies the current filter flags to waitingList and refreshes the adapter. */
    private void applyUserFilter() {
        visibleWaitingList.clear();

        boolean showAll = filterAllUsers;

        for (Pair<User, WaitingListState> entry : waitingList) {
            if (showAll) {
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

        // notify adapter that its backing list changed
        wLAdapter.notifyDataSetChanged();
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

        Runnable ensureAtLeastOneOn = () -> {
            if (!swAll.isChecked()
                    && !swEntered.isChecked()
                    && !swSelected.isChecked()
                    && !swCanceled.isChecked()) {
                swAll.setChecked(true);
            }
        };

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
                ensureAtLeastOneOn.run();
            }

            updating[0] = false;
        });

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
            filterAllUsers      = swAll.isChecked();
            filterEnteredUsers  = swEntered.isChecked();
            filterSelectedUsers = swSelected.isChecked();
            filterCanceledUsers = swCanceled.isChecked();

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
