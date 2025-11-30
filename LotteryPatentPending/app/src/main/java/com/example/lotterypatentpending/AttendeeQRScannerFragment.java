package com.example.lotterypatentpending;

import android.Manifest;
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

    /** The QR code scanner utility object. */
    private CodeScanner codeScanner;
    /**
     * A flag to prevent multiple fragment transactions from being triggered by a single scan.
     */
    private boolean launched = false;

    /**
     * An {@link ActivityResultLauncher} for requesting the CAMERA permission at runtime.
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
     */
    public AttendeeQRScannerFragment() {
        super(R.layout.attendee_fragment_qr_scanner);
    }
    /**
     * Called immediately after {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}
     * has returned. Checks for camera permission and initializes the scanner if permission is granted.
     *
     * @param view               The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state.
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
     * Defines the callback for successful QR code decoding, which validates the code,
     * fetches the event from Firebase, and navigates to the event details fragment.
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

                        if (!isAdded()) return;
                        //create the fragment with eventId
                        Fragment f = new AttendeeEventDetailsFragment();
                        // Launches new fragment
                        requireActivity().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.attendeeContainer, f)
                                .commit();

                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e("EventLoad","Failed to load EVENT", e);

                        if (isAdded()){
                            Toast.makeText(requireContext(), "Failed to load event", Toast.LENGTH_SHORT).show();
                            // Optionally restart scan:
                            if (codeScanner != null) codeScanner.startPreview();
                        }
                    }
                });

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
     * Starts the camera preview if the scanner is initialized.
     */
    @Override
    public void onResume() {
        super.onResume();
        launched = false;
        if (codeScanner != null) codeScanner.startPreview();
    }
    /**
     * Called when the Fragment is no longer resumed.
     * Releases camera resources to prevent memory leaks and allow other apps to use the camera.
     */
    @Override
    public void onPause() {
        if (codeScanner != null) codeScanner.releaseResources();
        super.onPause();
    }

}