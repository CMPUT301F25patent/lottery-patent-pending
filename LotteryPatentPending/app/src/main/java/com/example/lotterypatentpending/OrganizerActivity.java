package com.example.lotterypatentpending;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.lotterypatentpending.models.NotificationFactory;
import com.example.lotterypatentpending.models.NotificationRepository;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class OrganizerActivity extends AppCompatActivity {
    private NotificationRepository notifRepo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_organizer);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        notifRepo = new NotificationRepository();

        Button create_event = findViewById(R.id.create_event);
        Button view_events = findViewById(R.id.view_events);

        var current = FirebaseAuth.getInstance().getCurrentUser();

    }
    // Call this when the organizer clicks the "Send" button.
    private void sendMessageToSelectedUsers(String organizerId,
                                            List<String> selectedUserIds,
                                            String title, String body) {
        var n = NotificationFactory.custom(organizerId, title, body, selectedUserIds);
        notifRepo.createAndFanOut(n)
                .addOnSuccessListener(v -> Toast.makeText(this, "Notification sent", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> {
                    Log.e("Organizer", "sendMessageToSelectedUsers", e);
                    Toast.makeText(this, "Failed to send", Toast.LENGTH_SHORT).show();
                });
    }
}