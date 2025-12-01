package com.example.lotterypatentpending;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.lotterypatentpending.data.FirestoreUsersDataSource;
import com.example.lotterypatentpending.data.UserDataSource;
import com.example.lotterypatentpending.domain.OrganizerNotifier;
import com.example.lotterypatentpending.models.FirestoreNotificationRepository;
import com.example.lotterypatentpending.models.LotteryResultNotifier;
import com.example.lotterypatentpending.viewModels.OrganizerViewModel;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class OrganizerActivity extends AppCompatActivity {
    private OrganizerNotifier organizerNotifier;
    private OrganizerViewModel organizerVm;
    private final LotteryResultNotifier resultNotifier = new LotteryResultNotifier();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_activity_host);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavOrganizer);
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.organizer_nav_host);
        NavController navController = navHostFragment.getNavController();

        bottomNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_back) {
                navController.popBackStack();
                return true;
            }
            return false;
        });

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (destination.getId() == R.id.CreateEditEventFragment) {
                boolean isEdit = arguments != null && arguments.getBoolean("isEdit", false);
                toolbar.setTitle(isEdit ? "Edit Event" : "Create Event");
                return;
            }
            if (destination.getLabel() != null) {
                toolbar.setTitle(destination.getLabel());
            }
        });

        // --- FIX: Instantiation with 2 arguments ---
        organizerNotifier = new OrganizerNotifier(
                new FirestoreNotificationRepository(),
                new FirestoreUsersDataSource()
        );

        organizerVm = new ViewModelProvider(this).get(OrganizerViewModel.class);
    }
}
