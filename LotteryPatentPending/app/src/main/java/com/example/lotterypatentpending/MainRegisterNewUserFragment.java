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
import com.example.lotterypatentpending.models.UserEventRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * The create user fragment that appears and adds new user to DB
 * @maintainer Erik
 * @author Erik
 */
public class MainRegisterNewUserFragment extends Fragment {
    public interface OnProfileSaved { void onProfileSaved(); }

    private EditText nameEt, emailEt, phoneEt;
    private Button saveBtn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_user, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        nameEt = v.findViewById(R.id.et_name);
        emailEt = v.findViewById(R.id.et_email);
        phoneEt = v.findViewById(R.id.et_phone);
        saveBtn = v.findViewById(R.id.btn_save);

        saveBtn.setOnClickListener(view -> saveNewUser());
    }

    private void saveNewUser() {
        String name  = nameEt.getText().toString().trim();
        String email = emailEt.getText().toString().trim();
        String phone = phoneEt.getText().toString().trim();

        boolean ok = true;
        if (TextUtils.isEmpty(name))  { nameEt.setError("Required");  ok = false; }
        if (TextUtils.isEmpty(email)) { emailEt.setError("Required"); ok = false; }
        if (TextUtils.isEmpty(phone)) { phoneEt.setError("Required"); ok = false; }
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
