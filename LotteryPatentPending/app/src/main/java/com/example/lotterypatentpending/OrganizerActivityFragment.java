package com.example.lotterypatentpending;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.lotterypatentpending.models.User;
import com.example.lotterypatentpending.viewModels.EventViewModel;
import com.example.lotterypatentpending.viewModels.UserEventRepository;

/**
 * Fragment that serves as the main organizer dashboard.
 * <p>
 * Provides buttons for creating events, viewing events, and navigating back to the home screen.
 * </p>
 * <p>
 * This fragment is typically hosted within the OrganizerActivity.
 * </p>
 *
 * @author Ebuka
 */
public class OrganizerActivityFragment extends Fragment {

    Button create_event;
    Button view_events;
    TextView greeting;


    /**
     * Inflates the fragment layout.
     *
     * @param inflater           LayoutInflater object
     * @param container          Parent view that the fragment's UI should attach to
     * @param savedInstanceState Bundle containing saved state
     * @return the root view of the fragment layout
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.organizer_fragment_organizer_activity, container, false);

    }

    /**
     * Called immediately after the view is created.
     * <p>
     * Initializes buttons and sets up click listeners for navigation and finishing the activity.
     * </p>
     *
     * @param v                  The view returned by onCreateView
     * @param savedInstanceState Bundle containing saved state
     */
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        create_event = v.findViewById(R.id.create_event);
        view_events = v.findViewById(R.id.view_events);
        greeting = v.findViewById(R.id.organizer_greeting);

        User user = UserEventRepository.getInstance().getUser().getValue();
        if (user != null) {
            greeting.setText("Welcome, " + user.getName());
        }

        create_event.setOnClickListener(view -> {
            EventViewModel viewModel = new ViewModelProvider(requireActivity()).get(EventViewModel.class);
            viewModel.setEvent(null);

            NavHostFragment.findNavController(OrganizerActivityFragment.this)
                    .navigate(R.id.action_main_to_createEvent);

        });

        view_events.setOnClickListener(view -> {
            NavHostFragment.findNavController(OrganizerActivityFragment.this)
                    .navigate(R.id.action_main_to_viewEventsList);
        });

    }
}
