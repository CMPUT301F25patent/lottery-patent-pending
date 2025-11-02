package com.example.lotterypatentpending;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AttendeeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendee);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);

        // home button in header: go back to Main
        toolbar.setNavigationIcon(R.drawable.ic_home);
        toolbar.setNavigationOnClickListener(v -> finish());

        // default tab = events
        setTitle("Events");
        load(new AttendeeEventsFragment());

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_events) {
                setTitle("Events");
                return load(new AttendeeEventsFragment());
            }
            if (id == R.id.nav_profile) {
                setTitle("Profile");
                return load(new AttendeeProfileFragment());
            }
            if (id == R.id.nav_scan) {
                setTitle("Scan");
                return load(new QRScannerFragment());
            }
            return false;
        });
    }

    private boolean load(@NonNull Fragment f) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.attendeeContainer, f)
                .commit();
        return true;
    }

    private void setTitle(String t) {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(t);
    }
}
