package com.example.lotterypatentpending;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.example.lotterypatentpending.models.Event;
import com.example.lotterypatentpending.models.FirebaseManager;
import com.example.lotterypatentpending.models.QRCode;
import com.example.lotterypatentpending.viewModels.UserEventRepository;

/**
 * A fragment responsible for handling QR code scanning for attendees.
 * This class uses the device's camera to scan a QR code, validates if it's an event QR code
 * for this application, fetches the corresponding event details from Firebase, and then
 * navigates the user to the {@link AttendeeEventDetailsFragment}.
 * <p>
 * It handles runtime camera permission requests and manages the lifecycle of the code scanner
 * to ensure resources are properly released.
 *
 * @maintainer Erik
 * @author Erik
 */
public class AttendeeQRScannerFragment extends Fragment {

    private CodeScanner codeScanner;
    /**
     * A flag to prevent multiple fragment transactions from being triggered by a single scan.
     */
    private boolean launched = false;

    /**
     * An ActivityResultLauncher for requesting the CAMERA permission at runtime.
     * If the permission is granted, the scanner is started. If denied, a toast message is shown.
     */
    private final ActivityResultLauncher<String> askCamera =
            registerForActivityResult(new RequestPermission(), isGranted -> {
                if (isGranted) startScanner();
                else
                    Toast.makeText(requireContext(), "Camera permission required", Toast.LENGTH_SHORT).show();
            });

    /**
     * Default constructor.
     * Passes the layout resource for this fragment to the superclass constructor.
     */
    public AttendeeQRScannerFragment() {
        super(R.layout.fragment_attendee_qr_scanner);
    }
    /**
     * Called immediately after {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}
     * has returned, but before any saved state has been restored in to the view.
     * This is where the camera permission check and scanner initialization are triggered.
     *
     * @param view               The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     *                           from a previous saved state as given in the Bundle.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        launched = false;

        // Permission check
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startScanner();
        } else {
            askCamera.launch(Manifest.permission.CAMERA);
        }
    }
    /**
     * Initializes and starts the {@link CodeScanner}.
     * This method sets up the scanner view, defines the callback for when a QR code is successfully
     * decoded, and handles the subsequent logic of validating the QR code and navigating to the
     * event details.
     */
    private void startScanner() {
        View v = requireView();
        CodeScannerView scannerView = v.findViewById(R.id.scanner_view);
        // Use activity as lifecycle owner/context for camera
        codeScanner = new CodeScanner(requireActivity(), scannerView);

        codeScanner.setDecodeCallback(result -> requireActivity().runOnUiThread(() -> {

            if (launched) return;
            launched = true;

            String text = result.getText();              // e.g., "EVT:abc123"
            QRCode qr = QRCode.fromContent(text);       // -> QRCode or null

            if (qr != null) {
                String eventId = qr.getEventId();

                // Tell the host activity to show the event tab (or navigate)
                if (codeScanner != null) {
                    codeScanner.releaseResources();
                    codeScanner = null;
                }
                Toast.makeText(requireContext(), "QR scanned", Toast.LENGTH_SHORT).show();

                //get instances of firebase
                FirebaseManager fm = FirebaseManager.getInstance();

                fm.getEvent(eventId, new FirebaseManager.FirebaseCallback<Event>() {
                    @Override
                    public void onSuccess(Event result) {
                        UserEventRepository.getInstance().setEvent(result);

                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e("EventLoad","Failed to load EVENT", e);
                    }
                });

                //create the fragment with eventId
                Fragment f = new AttendeeEventDetailsFragment();
                // Launches new fragment
                    requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.attendeeContainer, f)
                        .commit();
            } else {
                Toast.makeText(requireContext(), "Not an event QR for this app", Toast.LENGTH_SHORT).show();
                codeScanner.startPreview();
            }
        }));

        // Tap to refocus / restart preview
        scannerView.setOnClickListener(v2 -> codeScanner.startPreview());
    }
    /**
     * Called when the fragment is visible to the user and actively running.
     * This is a good place to start the camera preview.
     */
    @Override
    public void onResume() {
        super.onResume();
        launched = false;
        if (codeScanner != null) codeScanner.startPreview();
    }
    /**
     * Called when the Fragment is no longer resumed.
     * This is the time to release camera resources to prevent memory leaks and
     * allow other apps to use the camera.
     */
    @Override
    public void onPause() {
        if (codeScanner != null) codeScanner.releaseResources();
        super.onPause();
    }

}