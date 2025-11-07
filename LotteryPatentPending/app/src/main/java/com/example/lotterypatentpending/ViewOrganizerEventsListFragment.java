package com.example.lotterypatentpending;

import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.example.lotterypatentpending.models.Event;
import com.example.lotterypatentpending.models.FirebaseManager;
import com.example.lotterypatentpending.viewModels.EventViewModel;

/**
 * Fragment allowing organizers to view their event list (future
 * and search for a specific event by ID. (only functionality included so far)
 */
public class ViewOrganizerEventsListFragment extends Fragment {

    private ImageButton backButton;
    private EditText searchEventId;
    private ImageButton searchButton;
    private FirebaseManager fm;

    /**
     * Inflates the fragment's layout.
     *
     * @param inflater The LayoutInflater object that can be used to inflate any views.
     * @param container The parent ViewGroup.
     * @param savedInstanceState Bundle containing saved instance state.
     * @return The root View of the fragment's layout.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_view_organizer_events_list, container, false);
    }

    /**
     * Called after the view has been created.
     * Initializes UI elements, sets click listeners for buttons.
     *
     * @param v The root view of the fragment.
     * @param savedInstanceState Bundle containing saved instance state.
     */
    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        backButton = v.findViewById(R.id.btn_back);
        searchEventId = v.findViewById(R.id.et_search);
        searchButton = v.findViewById(R.id.btn_search);
        fm = FirebaseManager.getInstance();

        backButton.setOnClickListener(view -> NavHostFragment.findNavController(this).popBackStack());
        searchButton.setOnClickListener(view -> {
            getSearchedEvent();
            NavHostFragment.findNavController(ViewOrganizerEventsListFragment.this)
                    .navigate(R.id.action_viewEventsList_to_Event_View);
        });
    }

    /**
     * Retrieves an event from Firebase based on the text entered
     * in the search field and stores it in the shared EventViewModel.
     */
    public void getSearchedEvent(){
        String eventId = searchEventId.getText().toString().trim();
        fm.getEvent(eventId, new FirebaseManager.FirebaseCallback<Event>() {
            @Override
            public void onSuccess(Event result) {
                EventViewModel viewModel = new ViewModelProvider(requireActivity()).get(EventViewModel.class);
                viewModel.setEvent(result);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("EventLoad", "Failed to load EVENT", e);
            }
        });
    }
}
