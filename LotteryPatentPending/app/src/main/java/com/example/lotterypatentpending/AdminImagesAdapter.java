package com.example.lotterypatentpending;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lotterypatentpending.models.Event;

import java.util.List;

/**
 * Adapter for displaying event posters and titles in a RecyclerView for administrators.
 * It handles binding event data to the views and listens for user interactions.
 */
public class AdminImagesAdapter extends RecyclerView.Adapter<AdminImagesAdapter.ImageViewHolder> {

    /**
     * A listener interface for handling click and long-click events on items in the RecyclerView.
     * The hosting Fragment or Activity must implement this interface to respond to user input.
     */
    public interface OnImageClickListener {
        /**
         * Called when an image item is clicked.
         *
         * @param event The event object associated with the clicked item.
         */
        void onImageClick(Event event);

        /**
         * Called when an image item is long-clicked.
         *
         * @param event The event object associated with the long-clicked item.
         */
        void onImageLongClick(Event event);
    }

    private final List<Event> eventList;
    private final OnImageClickListener listener;

    /**
     * Constructs the adapter.
     *
     * @param eventList The list of events to display.
     * @param listener  The listener that will handle click events.
     */
    public AdminImagesAdapter(List<Event> eventList, OnImageClickListener listener) {
        this.eventList = eventList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_image_item, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Event event = eventList.get(position);
        holder.bind(event, listener);
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    /**
     * ViewHolder for an individual event image item.
     * It holds the views for the image and event name.
     */
    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView eventNameTextView;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            eventNameTextView = itemView.findViewById(R.id.eventNameTextView);
        }

        /**
         * Binds an event's data to the views in the ViewHolder.
         *
         * @param event    The event to display.
         * @param listener The listener to attach to the item view for click handling.
         */
        void bind(final Event event, final OnImageClickListener listener) {
            // Set event name
            if (event.getTitle() != null) {
                eventNameTextView.setText(event.getTitle());
            } else {
                eventNameTextView.setText("Untitled Event");
            }

            // Set image
            byte[] bytes = event.getPosterBytes();
            if (bytes != null && bytes.length > 0) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                imageView.setImageBitmap(bitmap);
            } else {
                imageView.setImageDrawable(null);
            }

            // Set the click and long-click listeners
            itemView.setOnClickListener(v -> listener.onImageClick(event));
            itemView.setOnLongClickListener(v -> {
                listener.onImageLongClick(event);
                return true; // long click
            });
        }
    }
}
