package com.example.lotterypatentpending;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
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
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.lotterypatentpending.adapters.WaitingListAdapter;
import com.example.lotterypatentpending.helpers.LoadingOverlay;
import com.example.lotterypatentpending.models.Event;
import com.example.lotterypatentpending.models.EventState;
import com.example.lotterypatentpending.models.FirebaseManager;
import com.example.lotterypatentpending.models.LotterySystem;
import com.example.lotterypatentpending.models.User;
import com.example.lotterypatentpending.models.WaitingList;
import com.example.lotterypatentpending.models.WaitingListState;
import com.example.lotterypatentpending.viewModels.EventViewModel;
import com.example.lotterypatentpending.viewModels.OrganizerViewModel;
import com.example.lotterypatentpending.viewModels.UserEventRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.firestore.ListenerRegistration;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

public class OrganizerViewWaitingListFragment extends Fragment {
    private MaterialButton sampleBtn;
    private Button cancelEntrantBtn, exportBtn;
    private EventViewModel evm;
    private OrganizerViewModel organizerViewModel;
    private FirebaseManager fm;
    private LoadingOverlay loading;
    private ArrayList<Pair<User, WaitingListState>> waitingList = new ArrayList<>();
    private ArrayList<Pair<User, WaitingListState>> visibleWaitingList = new ArrayList<>();
    private ListView waitinglistView;
    private WaitingListAdapter wLAdapter;
    private int selectedPosition = -1;
    private Pair<User, WaitingListState> selectedEntrant = null;
    private String csvContent;
    private PopupWindow userFilterPopup;
    private boolean filterAllUsers = true;
    private boolean filterEnrolledUsers = false;
    private boolean filterSelectedUsers = false;
    private boolean filterCanceledUsers = false;

    private ListenerRegistration eventLiveRegistration;

    private final ActivityResultLauncher<Intent> createCsvFileLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    writeToFile(uri, csvContent);
                }
            });

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.organizer_fragment_view_event_waiting_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        evm = new ViewModelProvider(requireActivity()).get(EventViewModel.class);
        organizerViewModel = new ViewModelProvider(requireActivity()).get(OrganizerViewModel.class);
        fm = FirebaseManager.getInstance();

        waitinglistView = v.findViewById(R.id.waitingList);
        sampleBtn = v.findViewById(R.id.btn_lottery_sample);
        exportBtn = v.findViewById(R.id.organizer_event_waiting_list_button_export);
        cancelEntrantBtn = v.findViewById(R.id.organizer_event_waiting_list_button_cancel);

        ViewGroup root = v.findViewById(R.id.organizer_events_root);
        View overlayView = getLayoutInflater().inflate(R.layout.loading_screen, root, false);
        root.addView(overlayView);
        loading = new LoadingOverlay(overlayView, null);

        View filterIcon = v.findViewById(R.id.filterDropDown);
        if (filterIcon != null) filterIcon.setOnClickListener(this::showUserFilterPopup);

        // init waitingList & adapter
        if (evm.getEvent().getValue() != null &&
                evm.getEvent().getValue().getWaitingList() != null &&
                evm.getEvent().getValue().getWaitingList().getList() != null) {
            waitingList.clear();
            waitingList.addAll(evm.getEvent().getValue().getWaitingList().getList());
        }
        wLAdapter = new WaitingListAdapter(requireContext(), visibleWaitingList);
        waitinglistView.setAdapter(wLAdapter);
        applyUserFilter();

        waitinglistView.setOnItemClickListener((parent, view, position, id) -> {
            if (selectedPosition == position) {
                selectedPosition = -1;
                selectedEntrant = null;
                cancelEntrantBtn.setEnabled(false);
            } else {
                selectedPosition = position;
                selectedEntrant = visibleWaitingList.get(position);
                if (selectedEntrant.second == WaitingListState.SELECTED) {
                    cancelEntrantBtn.setEnabled(true);
                } else {
                    cancelEntrantBtn.setEnabled(false);
                    Toast.makeText(requireContext(), "Can only cancel SELECTED users.", Toast.LENGTH_SHORT).show();
                }
            }
            wLAdapter.setSelectedPosition(selectedPosition);
        });

        cancelEntrantBtn.setOnClickListener(v1 -> {
            if (selectedEntrant != null) cancelEntrantHelper(selectedEntrant);
            else Toast.makeText(requireContext(), "Select an entrant first.", Toast.LENGTH_SHORT).show();
        });

        exportBtn.setOnClickListener(v2 -> exportAcceptedToCSV());
        cancelEntrantBtn.setEnabled(false);

        evm.getEvent().observe(getViewLifecycleOwner(), event -> {
            if (event == null) return;

            updateButtons(event);
            fetchWaitingList(event.getId());

            // attach live Firestore listener once
            if (eventLiveRegistration == null) {
                eventLiveRegistration = fm.getEventLive(
                        event.getId(),
                        new FirebaseManager.FirebaseCallback<Event>() {
                            @Override
                            public void onSuccess(Event liveEvent) {
                                if (liveEvent == null) return;
                                // push live event into ViewModel -> retriggers this observer
                                evm.setEvent(liveEvent);
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Log.e("OrganizerWaitingList", "getEventLive failed", e);
                            }
                        }
                );
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (evm != null && evm.getEvent().getValue() != null) {
            fetchWaitingList(evm.getEvent().getValue().getId());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (eventLiveRegistration != null) {
            eventLiveRegistration.remove();
            eventLiveRegistration = null;
        }
    }

    private void sampleBtnHelper(Event event) {
        if (event == null) return;

        loading.show();

        String eventId = event.getId();
        String eventTitle = event.getTitle();

        User organizer = UserEventRepository.getInstance().getUser().getValue();
        if (organizer == null) {
            loading.hide();
            Toast.makeText(getContext(), "Organizer not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }
        String organizerId = organizer.getUserId();

        fm.getWaitingListPairs(eventId, new FirebaseManager.FirebaseCallback<List<Pair<User, WaitingListState>>>() {
            @Override
            public void onSuccess(List<Pair<User, WaitingListState>> pairs) {
                if (pairs == null || pairs.isEmpty()) {
                    loading.hide();
                    Toast.makeText(getContext(), "No entrants to draw.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Ensure the Event has a WaitingList instance
                if (event.getWaitingList() == null) {
                    event.setWaitingList(new WaitingList());
                }

                //  1) Sync Firestore state → Event.waitingList
                event.getWaitingList().setList(new ArrayList<>(pairs));

                // 2) Run the domain lottery logic (capacity-aware)
                event.runLottery();  // uses capacity - takenSpots and sets SELECTED_ENTRANTS

                // Notify other observers that the Event has changed (state + waiting list)
                evm.setEvent(event);

                // Use the updated list from the Event as the single source of truth
                List<Pair<User, WaitingListState>> updatedPairs = event.getWaitingList().getList();

                //  3) Push updated user states to Firestore
                fm.updateWaitingListStates(eventId, updatedPairs, new FirebaseManager.FirebaseCallback<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                        //  4) Save the event (including updated waiting list + state)
                        fm.addOrUpdateEvent(eventId, event);

                        // Build ID lists for notifications from the updated data
                        List<String> allIds = new ArrayList<>();
                        List<String> winIds = new ArrayList<>();
                        for (Pair<User, WaitingListState> p : updatedPairs) {
                            if (p.first != null && p.first.getUserId() != null) {
                                allIds.add(p.first.getUserId());
                                if (p.second == WaitingListState.SELECTED) {
                                    winIds.add(p.first.getUserId());
                                }
                            }
                        }

                        organizerViewModel.publishResults(organizerId, eventId, eventTitle, allIds, winIds)
                                .addOnSuccessListener(v -> {
                                    loading.hide();
                                    Toast.makeText(getContext(), "Draw complete! Notifications sent.", Toast.LENGTH_LONG).show();
                                    fetchWaitingList(eventId);   // refresh UI from backend
                                })
                                .addOnFailureListener(e -> {
                                    loading.hide();
                                    Toast.makeText(getContext(), "Notifications failed.", Toast.LENGTH_LONG).show();
                                    fetchWaitingList(eventId);
                                });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        loading.hide();
                        Toast.makeText(getContext(), "Failed to save results.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                loading.hide();
                Toast.makeText(getContext(), "Failed to load entrants.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateButtons(Event currentEvent) {
        sampleBtn.setVisibility(View.GONE);
        exportBtn.setVisibility(View.GONE);
        EventState state = currentEvent.getEventState();
        if (state == EventState.CLOSED_FOR_REG || state == EventState.SELECTED_ENTRANTS) {
            sampleBtn.setVisibility(View.VISIBLE);
            sampleBtn.setText(state == EventState.CLOSED_FOR_REG ? "Draw Attendants" : "Redraw Attendants");
            sampleBtn.setOnClickListener(v -> sampleBtnHelper(currentEvent));
        }
        if (state == EventState.SELECTED_ENTRANTS || state == EventState.CONFIRMED_ENTRANTS || state == EventState.CANCELLED || state == EventState.CLOSED_FOR_REG || state == EventState.ENDED) {
            exportBtn.setVisibility(View.VISIBLE);
        }
    }

    private void exportAcceptedToCSV(){
        WaitingList wl = evm.getEvent().getValue().getWaitingList();
        saveCsv(wl.exportAcceptedEntrantsToCsv(), "accepted_attendees.csv");
    }

    private void saveCsv(String csvContent, String title) {
        this.csvContent = csvContent;
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.setType("text/csv");
        intent.putExtra(Intent.EXTRA_TITLE, title);
        createCsvFileLauncher.launch(intent);
    }

    private void writeToFile(Uri uri, String csv) {
        try (OutputStream os = requireContext().getContentResolver().openOutputStream(uri)) {
            os.write(csv.getBytes(StandardCharsets.UTF_8));
            os.flush();
            Toast.makeText(requireContext(), "CSV exported!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Error exporting CSV", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchWaitingList(String eventId) {
        fm.getEventWaitingList(eventId, new FirebaseManager.FirebaseCallback<ArrayList<Pair<User, WaitingListState>>>() {
            @Override
            public void onSuccess(ArrayList<Pair<User, WaitingListState>> result) {
                waitingList.clear();
                if (result != null) waitingList.addAll(result);

                // Keep Event's WaitingList in sync with Firestore
                Event e = evm.getEvent().getValue();
                if (e != null) {
                    if (e.getWaitingList() == null) {
                        e.setWaitingList(new WaitingList());
                    }
                    e.getWaitingList().setList(new ArrayList<>(waitingList));
                    // ⚠ Don't call evm.setEvent(e) here, or you'll re-trigger the observer and
                    // loop into fetchWaitingList() again. Just mutate in place.
                }

                applyUserFilter();
                if (loading != null) loading.hide();
            }

            @Override
            public void onFailure(Exception e) {
                if (loading != null) loading.hide();
            }
        });
    }

    private void cancelEntrantHelper(Pair<User, WaitingListState> entrant) {
        if (entrant == null) return;

        // 1) Guard: only allow cancelling SELECTED entrants
        if (entrant.second != WaitingListState.SELECTED) {
            Toast.makeText(
                    requireContext(),
                    "You can only cancel entrants who are in the SELECTED state.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        loading.show();

        Event currentEvent = evm.getEvent().getValue();
        if (currentEvent == null || currentEvent.getWaitingList() == null) {
            Toast.makeText(requireContext(),
                    "Event or waiting list not available.",
                    Toast.LENGTH_SHORT).show();
            if (loading != null) loading.hide();
            return;
        }

        User userToCancel = entrant.first;
        String eventId = currentEvent.getId();

        // 2) Find this entrant in the master waitingList
        int index = -1;
        for (int i = 0; i < waitingList.size(); i++) {
            Pair<User, WaitingListState> p = waitingList.get(i);
            if (p.first != null &&
                    p.first.getUserId() != null &&
                    p.first.getUserId().equals(userToCancel.getUserId())) {
                index = i;
                break;
            }
        }

        if (index == -1) {
            if (loading != null) loading.hide();
            Toast.makeText(requireContext(),
                    "Entrant not found in list.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // 3) Double-check current state from the master list
        WaitingListState currentState = waitingList.get(index).second;
        if (currentState != WaitingListState.SELECTED) {
            Toast.makeText(
                    requireContext(),
                    "You can only cancel entrants who are in the SELECTED state.",
                    Toast.LENGTH_SHORT
            ).show();
            if (loading != null) loading.hide();
            return;
        }

        // 4) Mark as CANCELED locally
        waitingList.set(index, new Pair<>(userToCancel, WaitingListState.CANCELED));

        // Clear selection + update UI
        selectedPosition = -1;
        selectedEntrant = null;
        cancelEntrantBtn.setEnabled(false);
        wLAdapter.setSelectedPosition(selectedPosition);

        // 5) Push updated list into Event + ViewModel
        currentEvent.getWaitingList().setList(waitingList);
        evm.setEvent(currentEvent);

        // 6) Update Firestore
        fm.updateEntrantState(eventId, userToCancel.getUserId(), WaitingListState.CANCELED);

        // 7) Rebuild visible list with current filters
        applyUserFilter();

        if (loading != null) loading.hide();
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
            if (filterEnrolledUsers && state == WaitingListState.ACCEPTED) include = true;
            if (filterSelectedUsers && state == WaitingListState.SELECTED) include = true;
            if (filterCanceledUsers && state == WaitingListState.CANCELED) include = true;
            if (include) visibleWaitingList.add(entry);
        }
        selectedPosition = -1;
        selectedEntrant = null;
        cancelEntrantBtn.setEnabled(false);
        wLAdapter.setSelectedPosition(selectedPosition);
        wLAdapter.notifyDataSetChanged();
    }

    private void showUserFilterPopup(View anchor) {
        if (userFilterPopup != null && userFilterPopup.isShowing()) {
            userFilterPopup.dismiss();
            return;
        }
        View content = LayoutInflater.from(requireContext()).inflate(R.layout.organizer_user_popup_filter, null, false);
        userFilterPopup = new PopupWindow(content, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        userFilterPopup.setOutsideTouchable(true);
        userFilterPopup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        SwitchMaterial swAll = content.findViewById(R.id.browseAllUsers);
        SwitchMaterial swEnrolled = content.findViewById(R.id.browseEnrolledUsers);
        SwitchMaterial swSelected = content.findViewById(R.id.browseSelectedUsers);
        SwitchMaterial swCanceled = content.findViewById(R.id.browseCanceledUsers);
        swAll.setChecked(filterAllUsers);
        swEnrolled.setChecked(filterEnrolledUsers);
        swSelected.setChecked(filterSelectedUsers);
        swCanceled.setChecked(filterCanceledUsers);
        final boolean[] updating = {false};
        Runnable ensureAtLeastOneOn = () -> {
            if (!swAll.isChecked() && !swEnrolled.isChecked() && !swSelected.isChecked() && !swCanceled.isChecked()) {
                swAll.setChecked(true);
            }
        };
        swAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (updating[0]) return;
            updating[0] = true;
            filterAllUsers = isChecked;
            if (isChecked) {
                filterEnrolledUsers = filterSelectedUsers = filterCanceledUsers = false;
                swEnrolled.setChecked(false); swSelected.setChecked(false); swCanceled.setChecked(false);
            } else ensureAtLeastOneOn.run();
            updating[0] = false;
        });
        swEnrolled.setOnCheckedChangeListener((v, c) -> { if(!updating[0]) { updating[0]=true; filterEnrolledUsers=c; if(c) { filterAllUsers=false; swAll.setChecked(false);} else ensureAtLeastOneOn.run(); updating[0]=false; }});
        swSelected.setOnCheckedChangeListener((v, c) -> { if(!updating[0]) { updating[0]=true; filterSelectedUsers=c; if(c) { filterAllUsers=false; swAll.setChecked(false);} else ensureAtLeastOneOn.run(); updating[0]=false; }});
        swCanceled.setOnCheckedChangeListener((v, c) -> { if(!updating[0]) { updating[0]=true; filterCanceledUsers=c; if(c) { filterAllUsers=false; swAll.setChecked(false);} else ensureAtLeastOneOn.run(); updating[0]=false; }});
        userFilterPopup.setOnDismissListener(() -> {
            filterAllUsers = swAll.isChecked();
            filterEnrolledUsers = swEnrolled.isChecked();
            filterSelectedUsers = swSelected.isChecked();
            filterCanceledUsers = swCanceled.isChecked();
            if (!filterAllUsers && !filterEnrolledUsers && !filterSelectedUsers && !filterCanceledUsers) filterAllUsers = true;
            applyUserFilter();
        });
        userFilterPopup.showAsDropDown(anchor, 0, 0);
    }
}
