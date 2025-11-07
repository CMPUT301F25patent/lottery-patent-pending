package com.example.lotterypatentpending;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

public class OrganizerActivityFragment extends Fragment {

    Button create_event;
    Button view_events;
//    Button back_button;
    ImageButton home_button;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_organizer_activity, container, false);

    }

    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        create_event = v.findViewById(R.id.create_event);
        view_events = v.findViewById(R.id.view_events);
        home_button = v.findViewById(R.id.homeButton);

        create_event.setOnClickListener(view -> {
            NavHostFragment.findNavController(OrganizerActivityFragment.this)
                    .navigate(R.id.action_main_to_createEvent);
        });

        home_button.setOnClickListener(view -> {
            requireActivity().finish();
        });
    }
}
