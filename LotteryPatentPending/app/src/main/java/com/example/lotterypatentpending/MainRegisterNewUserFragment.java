package com.example.lotterypatentpending;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.lotterypatentpending.models.FirebaseManager;
import com.example.lotterypatentpending.models.User;
import com.example.lotterypatentpending.viewModels.UserEventRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Fragment for a new user to register their profile details (name, email, phone)
 * after initial sign-in (typically anonymous). This data is then saved to Firestore.
 * @author Erik
 * @contributor  Erik, Michael
 * The create user fragment that appears and adds new user to DB
 */
public class MainRegisterNewUserFragment extends Fragment {
    /** Interface for the hosting activity to implement, called when the profile is successfully saved. */
    public interface OnProfileSaved {
        /**
         * Called by the fragment to notify the host activity that the user profile has been saved.
         */
        void onProfileSaved();
    }

    /** EditText for the user's name. */
    private EditText nameEt;
    /** EditText for the user's email. */
    private EditText emailEt;
    /** EditText for the user's phone number/contact info. */
    private EditText phoneEt;
    /** Button to trigger the user profile saving process. */
    private Button saveBtn;

    /**
     * Inflates the fragment layout.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.main_fragment_create_user, container, false);
    }

    /**
     * Initializes the UI views and sets the click listener for the save button.
     */
    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        nameEt = v.findViewById(R.id.et_name);
        emailEt = v.findViewById(R.id.et_email);
        phoneEt = v.findViewById(R.id.et_phone);
        saveBtn = v.findViewById(R.id.btn_save);

        saveBtn.setOnClickListener(view -> saveNewUser());
    }

    /**
     * Validates input fields, creates a new {@link User} object, saves it to Firestore,
     * sets it in the {@link UserEventRepository}, and notifies the host activity.
     */
    private void saveNewUser() {
        String name  = nameEt.getText().toString().trim();
        String email = emailEt.getText().toString().trim();
        String phone = phoneEt.getText().toString().trim();

        boolean ok = true;
        if (TextUtils.isEmpty(name))  { nameEt.setError("Required");  ok = false; }
        if (TextUtils.isEmpty(email)) { emailEt.setError("Required"); ok = false; }
        if (!ok) return;

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(requireContext(), "Not signed in", Toast.LENGTH_LONG).show();
            return;
        }

        // create new user
        String authUid = currentUser.getUid();
        User user = new User(authUid, name, email, phone, false);

        // set global user instance
        UserEventRepository.getInstance().setUser(user);

        saveBtn.setEnabled(false);

        FirebaseManager.getInstance().addOrUpdateUser(user);

        Toast.makeText(requireContext(), "Profile saved", Toast.LENGTH_SHORT).show();
        if (getActivity() instanceof OnProfileSaved) {
            ((OnProfileSaved) getActivity()).onProfileSaved();
        }
    }
}