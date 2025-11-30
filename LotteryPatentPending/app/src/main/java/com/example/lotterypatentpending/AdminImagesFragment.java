package com.example.lotterypatentpending;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lotterypatentpending.models.Event;
import com.google.firebase.firestore.Blob;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment that displays a grid of event posters for an administrator.
 * It allows the admin to view posters in full screen by clicking
 * and to delete posters by long-clicking.
 */
public class AdminImagesFragment extends Fragment implements AdminImagesAdapter.OnImageClickListener {

    /** Displays the grid of event posters. */
    private RecyclerView recyclerView;
    /** Adapter to bind {@link Event} data to the {@link RecyclerView}. */
    private AdminImagesAdapter adapter;
    /** List holding {@link Event} objects that have a poster. */
    private List<Event> eventList;
    /** Progress bar shown while fetching data. */
    private ProgressBar progressBar;
    /** Firestore database instance. */
    private FirebaseFirestore db;

    /**
     * Inflates the fragment's layout and initializes its views and data.
     *
     * @param inflater           The LayoutInflater object.
     * @param container          The parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     * @return The View for the fragment's UI.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.admin_fragment_images, container, false);

        db = FirebaseFirestore.getInstance();
        recyclerView = view.findViewById(R.id.imagesRecyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        eventList = new ArrayList<>();

        setupRecyclerView();
        fetchEventsWithPosters();

        return view;
    }

    /**
     * Configures the RecyclerView with a {@link GridLayoutManager} and sets up the adapter.
     */
    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        adapter = new AdminImagesAdapter(eventList, this);
        recyclerView.setAdapter(adapter);
    }

    /**
     * Handles a click on an image item. Displays the image in a full-screen dialog.
     *
     * @param event The event corresponding to the clicked image.
     */
    @Override
    public void onImageClick(Event event) {
        if (getContext() == null || event.getPosterBytes() == null || event.getPosterBytes().length == 0) {
            return;
        }

        // Create a container layout to hold the ImageView and provide padding
        FrameLayout container = new FrameLayout(getContext());
        container.setPadding(30, 30, 30, 30);
        ImageView fullImageView = new ImageView(getContext());

        fullImageView.setAdjustViewBounds(true);
        fullImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        fullImageView.setContentDescription("Full-size event poster");

        // Decode the byte array and set the image
        Bitmap bitmap = BitmapFactory.decodeByteArray(event.getPosterBytes(), 0, event.getPosterBytes().length);
        fullImageView.setImageBitmap(bitmap);
        container.addView(fullImageView);

        // create a dialog and set the CONTAINER as its content
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(container); // Set the container, which has minor padding

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    /**
     * Handles a long-click on an image item. Shows a confirmation dialog to delete the poster.
     *
     * @param event The event corresponding to the long-clicked image.
     */
    @Override
    public void onImageLongClick(Event event) {
        if (getContext() == null) return;

        new AlertDialog.Builder(getContext())
                .setTitle("Delete Image")
                .setMessage("Are you sure you want to delete the poster for '" + event.getTitle() + "'?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteImage(event);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Deletes an event's poster by setting its 'posterBlob' field to null in Firestore.
     *
     * @param eventToDelete The event whose poster should be deleted.
     */
    private void deleteImage(Event eventToDelete) {
        if (eventToDelete.getId() == null || eventToDelete.getId().isEmpty()) {
            Toast.makeText(getContext(), "Error: Event ID is missing.", Toast.LENGTH_SHORT).show();
            return;
        }

        // To "delete" the image, we set the posterBlob field to null
        db.collection("events").document(eventToDelete.getId())
                .update("posterBlob", null)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Image deleted.", Toast.LENGTH_SHORT).show();
                    // Remove from the local list and update the UI
                    int position = eventList.indexOf(eventToDelete);
                    if (position != -1) {
                        eventList.remove(position);
                        adapter.notifyItemRemoved(position);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to delete image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("AdminImagesFragment", "Error deleting image", e);
                });
    }

    /**
     * Fetches all events from Firestore that have a non-null poster image.
     */
    private void fetchEventsWithPosters() {
        progressBar.setVisibility(View.VISIBLE);

        db.collection("events")
                .whereNotEqualTo("posterBlob", null)
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful() && task.getResult() != null) {
                        eventList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Event event = new Event();
                            event.setId(document.getId());
                            event.setTitle(document.getString("title"));

                            Blob posterBlob = document.getBlob("posterBlob");
                            if (posterBlob != null) {
                                event.setPosterBytes(posterBlob.toBytes());
                            }
                            eventList.add(event);
                        }
                        adapter.notifyDataSetChanged();

                        if (eventList.isEmpty()) {
                            Toast.makeText(getContext(), "No event posters found.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "Error fetching images.", Toast.LENGTH_SHORT).show();
                        Log.e("AdminImagesFragment", "Error fetching events with posters", task.getException());
                    }
                });
    }
}