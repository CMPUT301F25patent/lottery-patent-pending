package com.example.lotterypatentpending;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private String fid; //device id


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        findViewById(R.id.btnAttendee).setOnClickListener(v ->
                startActivity(new Intent(this, AttendeeActivity.class)));

        findViewById(R.id.btnOrganizer).setOnClickListener(v ->
                startActivity(new Intent(this, OrganizerActivity.class)));

        findViewById(R.id.btnAdmin).setOnClickListener(v ->
                startActivity(new Intent(this, AdminActivity.class)));

        FirebaseApp.initializeApp(this);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        //Ensure anonymous auth
        if (auth.getCurrentUser() == null) {
            auth.signInAnonymously().addOnCompleteListener(t -> checkFID());
        } else {
            checkFID();
        }
    }

    private void checkFID() {
        FirebaseInstallations.getInstance().getId()
                .addOnSuccessListener(id -> {
                    fid = id;
                    db.collection("users").document(fid).get()
                            .addOnSuccessListener((DocumentSnapshot snap) -> {
                                if (!snap.exists()) showCreateUserFrame();
                            })
                            .addOnFailureListener(e -> showCreateUserFrame());
                })
                .addOnFailureListener(e -> {
                    showCreateUserFrame();
                });
    }

    private void showCreateUserFrame() {
        findViewById(R.id.createUserOverlay).setVisibility(View.VISIBLE);
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction()
                .replace(R.id.createUserOverlay, CreateUserFragment.newInstance(fid,
                        auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null))
                .commit();
    }

    @Override
    public void onProfileSaved() {
        getSupportFragmentManager().beginTransaction()
                .remove(getSupportFragmentManager().findFragmentById(R.id.createUserOverlay))
                .commit();
        findViewById(R.id.createUserOverlay).setVisibility(View.GONE);
    }
}