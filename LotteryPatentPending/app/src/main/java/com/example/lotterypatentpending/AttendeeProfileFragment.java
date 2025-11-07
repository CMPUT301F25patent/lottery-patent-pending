package com.example.lotterypatentpending;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lotterypatentpending.models.FirebaseManager;
import com.example.lotterypatentpending.models.User;
import com.example.lotterypatentpending.viewModels.UserEventRepository;
import com.google.firebase.auth.FirebaseAuth;

import org.jspecify.annotations.Nullable;


/**
 * Class AttendeeActivity
 * @author Erik
 * @contributor
 * Fragment for displaying and updating a logged-in attendee's profile.
 * Allows a user to update their name, email, and contact information, toggle notification preferences,
 * and delete their account.
 */

public class AttendeeProfileFragment extends Fragment {
    /** Text field for displaying and editing the user's name. */
    private TextView name;
    /** Text field for displaying and editing the user's email. */
    private TextView email;
    /** Text field for displaying and editing the user's phone number. */
    private TextView phone;
    /** Switch to enable or disable event notifications for the user. */
    private SwitchCompat notificationsSwitch;
    /** Button to save profile updates. */
    private Button saveBtn;
    /** Current logged-in user object. */
    private User user;

    /**
     * Default constructor that inflates the attendee profile layout.
     */
    public AttendeeProfileFragment(){
        super(R.layout.fragment_attendee_profile);
    }


    @Override
    public void onViewCreated(@NonNull View v, Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        //Set the buttons and TextView to ids
        name = v.findViewById(R.id.attendeeName);
        email = v.findViewById(R.id.attendeeEmail);
        phone = v.findViewById(R.id.attendeePhone);
        saveBtn = v.findViewById(R.id.profileSaveBtn);
        notificationsSwitch = v.findViewById(R.id.notifications);

        //Get activity for this fragment
        AttendeeActivity activity = (AttendeeActivity) requireActivity();

        //get user for fragment

        user = UserEventRepository.getInstance().getUser().getValue();

        name.setText(user.getName());
        email.setText(user.getEmail());
        phone.setText(user.getContactInfo());
        //set switch to current boolean of user
        if (user != null) {
            boolean enabled = user.isNotificationsOptIn();
            notificationsSwitch.setChecked(enabled);
            notificationsSwitch.setText(enabled ? "Notifications Enabled" : "Notifications Disabled");
        }
        //switch listener
        notificationsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (user == null) return;

            user.setNotificationsOptIn(isChecked);
            FirebaseManager.getInstance().addOrUpdateUser(user);

            notificationsSwitch.setText(isChecked ? "Notifications Enabled" : "Notifications Disabled");

            Toast.makeText(requireContext(),
                    isChecked ? "Notifications turned ON" : "Notifications turned OFF",
                    Toast.LENGTH_SHORT
            ).show();
        });

//        //save button listener
        saveBtn.setOnClickListener(btnView -> {
            user.setName(name.getText().toString());
            user.setEmail(email.getText().toString());
            user.setContactInfo(phone.getText().toString());
            FirebaseManager.getInstance().addOrUpdateUser(user);

            Toast.makeText(requireContext(), "Profile saved", Toast.LENGTH_SHORT).show();

        });
        Button deleteBtn;
        deleteBtn = v.findViewById(R.id.profileDeleteBtn);
        deleteBtn.setOnClickListener(view -> {
            if (user == null) {
                Toast.makeText(requireContext(), "No user loaded", Toast.LENGTH_SHORT).show();
                return;
            }

            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Delete Account")
                    .setMessage("Are you sure you want to delete this account? This action cannot be undone.")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        FirebaseManager.getInstance().deleteUser(user.getUserId());
                        Toast.makeText(requireContext(), "User deleted", Toast.LENGTH_SHORT).show();
                        FirebaseAuth.getInstance().signOut();

                        Intent intent = new Intent(requireContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);

                        requireActivity().finish();
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .show();
        });

    }
}
