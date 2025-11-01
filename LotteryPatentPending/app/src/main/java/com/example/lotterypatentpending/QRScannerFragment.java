package com.example.lotterypatentpending;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
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
import com.example.lotterypatentpending.models.QRCode;

public class QRScannerFragment extends Fragment {

    private CodeScanner codeScanner;

    // Ask for CAMERA at runtime
    private final ActivityResultLauncher<String> askCamera =
            registerForActivityResult(new RequestPermission(), isGranted -> {
                if (isGranted) startScanner();
                else Toast.makeText(requireContext(), "Camera permission required", Toast.LENGTH_SHORT).show();
            });

    public QRScannerFragment() {
        super(R.layout.fragment_qr_scanner);
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Permission check
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startScanner();
        } else {
            askCamera.launch(Manifest.permission.CAMERA);
        }
    }

    private void startScanner() {
        View v = requireView();
        CodeScannerView scannerView = v.findViewById(R.id.scanner_view);
        // Use activity as lifecycle owner/context for camera
        codeScanner = new CodeScanner(requireActivity(), scannerView);

        codeScanner.setDecodeCallback(result -> requireActivity().runOnUiThread(() -> {
            String text = result.getText();              // e.g., "EVT:abc123"
            QRCode qr = QRCode.fromPayload(text);       // -> QRCode or null
            if (qr != null) {
                String eventId = qr.getEventId();
                // Tell the host activity to show the event tab (or navigate)
                if (getActivity() instanceof ScannerResultHandler) {
                    ((ScannerResultHandler) getActivity()).onQrScanned(eventId);
                } else {
                    Toast.makeText(requireContext(), "Scanned: " + eventId, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(requireContext(), "Not an event QR for this app", Toast.LENGTH_SHORT).show();
                codeScanner.startPreview();
            }
        }));

        // Tap to refocus / restart preview
        scannerView.setOnClickListener(v2 -> codeScanner.startPreview());
    }

    @Override public void onResume() {
        super.onResume();
        if (codeScanner != null) codeScanner.startPreview();
    }

    @Override public void onPause() {
        if (codeScanner != null) codeScanner.releaseResources();
        super.onPause();
    }

    // Optional: interface so host activity can react to scan
    public interface ScannerResultHandler {
        void onQrScanned(@NonNull String eventId);
    }
}
