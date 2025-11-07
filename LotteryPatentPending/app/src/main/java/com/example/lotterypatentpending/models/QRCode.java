package com.example.lotterypatentpending.models;

/**
 * Small value class for event QR codes.
 * A QRCode just wraps an event ID and knows how to turn it into (and back from)
 * the string format we store in a QR image.
 *
 * Format used: "EVT:<eventId>".
 * At the moment this only supports event QR codes with a single fixed prefix.
 *
 * @author Erik
 * @maintainer Erik
 */
public class QRCode {

    /**
     * Prefix used to mark QR codes that belong to this app's events.
     */
    private static final String PREFIX = "EVT:";

    /**
     * The event ID represented by this QR code (without the prefix).
     */
    private String eventId;

    /**
     * Creates a new QRCode for the given event ID.
     *
     * @param eventId the event ID to associate with this QR code; must be non-null and not just whitespace
     * @throws IllegalArgumentException if eventId is null, empty, or only whitespace
     */
    public QRCode(String eventId) {
        if (eventId == null || eventId.trim().isEmpty()) {
            throw new IllegalArgumentException("eventId required");
        }
        this.eventId = eventId;
    }

    /**
     * Returns the event ID stored in this QR code.
     *
     * @return the event ID (without the prefix)
     */
    public String getEventId() {
        return this.eventId;
    }

    /**
     * Updates the event ID stored in this QR code.
     *
     * @param eventId the new event ID
     */
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    /**
     * Builds the string that will actually be encoded into the QR code.
     * This is just the prefix followed by the event ID.
     *
     * @return a payload string like "EVT:<eventId>"
     */
    public String toContent() {
        return PREFIX + eventId;
    }

    /**
     * Tries to create a QRCode from a payload string.
     * The string must start with the prefix ("EVT:") and have a non-empty
     * ID afterwards. If it does not match that format, this returns null.
     *
     * @param s the payload string, usually something produced by toContent()
     * @return a QRCode with the parsed event ID, or null if the string is invalid
     */
    public static QRCode fromContent(String s) {
        if (s == null || !s.startsWith(PREFIX)) {
            return null;
        }

        String id = s.substring(PREFIX.length()).trim();
        if (id.isEmpty()) {
            return null;
        }
        return new QRCode(id);
    }
}
