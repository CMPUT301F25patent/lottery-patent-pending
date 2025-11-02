package com.example.lotterypatentpending;

import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.lotterypatentpending.models.QRGenerator;


public class AttendeeEventsFragment extends Fragment {
    public AttendeeEventsFragment() {
        super(R.layout.fragment_attendee_events);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //EVERYTHING UNDER HERE CAN BE DELETED,
        //JUST CREATED QRCODE FOR TESTING
        ImageView qr = view.findViewById(R.id.qrPreview);
        String eventId = "abc123";
        QRGenerator.setQRToView(qr, eventId, 512);
    }
}