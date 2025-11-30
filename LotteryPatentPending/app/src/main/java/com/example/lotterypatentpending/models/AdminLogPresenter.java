package com.example.lotterypatentpending.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Pure helper for formatting/sorting admin logs for display.
 *
 *
 * @author Moffat
 * @maintainer Moffat
 */
public class AdminLogPresenter {
    /**
     * Formats the title for the admin log entry.
     * Example: "WAITLIST • evt1"
     */
    public static String formatTitle(NotificationLog n) {
        return n.getCategory().toString() + " • " + n.getEventId();
    }

    /**
     * Formats the body, truncating it to 100 characters if it's too long.
     */
    public static String formatBody(NotificationLog n) {
        String preview = n.getPayloadPreview();
        if (preview == null) {
            return "";
        }

        // The test expects a max length of 100, including the ellipsis.
        int maxLength = 100;
        if (preview.length() > maxLength) {
            // Subtract 1 for the ellipsis character.
            return preview.substring(0, maxLength - 1) + "…";
        }

        return preview;
    }

    /**
     * Formats the metadata including the time and organizer ID.
     * Example: "Sent at 03:04, org=org1"
     */
    public static String formatMeta(NotificationLog n) {
        if (n.getCreatedAt() == null) {
            return "Sent at ?, org=" + n.getOrganizerId();
        }
        // The test regex expects HH:mm format.
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String timeString = sdf.format(n.getCreatedAt());

        return "Sent at " + timeString + ", org=" + n.getOrganizerId();
    }
}

