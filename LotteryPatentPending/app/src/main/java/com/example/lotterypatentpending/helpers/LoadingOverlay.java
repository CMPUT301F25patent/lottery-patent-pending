package com.example.lotterypatentpending.helpers;


import android.view.View;

public class LoadingOverlay {
    private final View overlay;
    private final View content;

    public LoadingOverlay(View overlay, View content) {
        this.overlay = overlay;
        this.content = content;
    }

    public void show() {
        overlay.setVisibility(View.VISIBLE);
        if (content != null) content.setVisibility(View.GONE);
    }

    public void hide() {
        overlay.setVisibility(View.GONE);
        if (content != null) content.setVisibility(View.VISIBLE);
    }

    public View getView() {
        return overlay;
    }
}