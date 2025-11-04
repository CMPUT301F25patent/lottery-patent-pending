package com.example.lotterypatentpending;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.lotterypatentpending.models.User;


public class AttendeeProfileFragment extends Fragment {

    private User user;
    private TextView name;
    private TextView email;
    private TextView phone;
    private SwitchCompat notificationsSwitch;
    private Button saveBtn;


    @Override
    public void onViewCreated(@NonNull View v, Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        //Set the buttons and TextView to ids
        name = v.findViewById(R.id.attendeeName);
        email = v.findViewById(R.id.attendeeEmail);
        phone = v.findViewById(R.id.attendeePhone);
        saveBtn = v.findViewById(R.id.btn_save);
        notificationsSwitch = v.findViewById(R.id.notifications);

        //Get activity for this fragment
        AttendeeActivity activity = (AttendeeActivity) requireActivity();

        //get user from fragment
        user = activity.getUser();


        //switch logic and set view
        if (user != null) {
            notificationsSwitch.setChecked(user.isNotificationsOptIn());
        }
        notificationsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (user == null) return;
            user.setNotificationsOptIn(isChecked);

        });

    }
}