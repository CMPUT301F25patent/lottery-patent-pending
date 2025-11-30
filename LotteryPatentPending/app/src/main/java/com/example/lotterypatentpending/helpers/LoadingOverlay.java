package com.example.lotterypatentpending.helpers;
import android.view.View;

/**
 * An overlay that displays a loading screen
 * @author Erik
 * @contributor Erik
 */
public class LoadingOverlay {
    private final View overlay;
    private final View content;

    /**
     * Creates a LoadingOverlay
     * @param overlay The overlay view
     * @param content The main view that is to be hidden when loading
     */
    public LoadingOverlay(View overlay, View content) {
        this.overlay = overlay;
        this.content = content;
    }

    /**
     * Shows the overlay
     */
    public void show() {
        overlay.setVisibility(View.VISIBLE);
        if (content != null) content.setVisibility(View.GONE);
    }

    /**
     * Hides the overlay
     */
    public void hide() {
        overlay.setVisibility(View.GONE);
        if (content != null) content.setVisibility(View.VISIBLE);
    }

    /**
     * Gets the overlay View
     * @return overlay view
     */
    public View getView() {
        return overlay;
    }
}