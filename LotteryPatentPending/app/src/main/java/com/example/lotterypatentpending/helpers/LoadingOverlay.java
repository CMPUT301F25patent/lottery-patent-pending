package com.example.lotterypatentpending.helpers;
import android.view.View;

/**
 * Simple utility for toggling a full-screen or inline loading overlay.
 *
 * @author Erik
 */

public class LoadingOverlay {
    private final View overlay;
    private final View content;
    /**
     * @param overlay the loading view to show/hide
     * @param content an optional content view to hide while loading (may be null)
     */
    public LoadingOverlay(View overlay, View content) {
        this.overlay = overlay;
        this.content = content;
    }
    /** Shows the loading overlay and hides content (if provided). */
    public void show() {
        overlay.setVisibility(View.VISIBLE);
        if (content != null) content.setVisibility(View.GONE);
    }
    /** Hides the loading overlay and restores content visibility (if provided). */
    public void hide() {
        overlay.setVisibility(View.GONE);
        if (content != null) content.setVisibility(View.VISIBLE);
    }
    /** @return the overlay view itself. */
    public View getView() {
        return overlay;
    }
}