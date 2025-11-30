package com.example.lotterypatentpending.models;

// package com.example.lotterypatentpending.models;

/**
 * Represents a metadata record for an image file uploaded
 */
public class ImageRecord {
    private String id;          // Firestore doc id
    private String eventId;     // linked event
    private String storagePath; // path in Firebase Storage (e.g. "event_posters/<eventId>.jpg")
    private String uploaderId;  // organizer uid (optional)
    private long createdAt;     // millis since epoch

    /**
     * Empty constructor for Firebase
     */
    public ImageRecord() {}

    /**
     * Creates an ImageRecord
     * @param id Image ID
     * @param eventId Event ID this image is associated with
     * @param storagePath Storage path
     * @param uploaderId ID of user who uploaded
     * @param createdAt When this image record was created
     */
    public ImageRecord(String id, String eventId, String storagePath, String uploaderId, long createdAt) {
        this.id = id;
        this.eventId = eventId;
        this.storagePath = storagePath;
        this.uploaderId = uploaderId;
        this.createdAt = createdAt;
    }

    /**
     * Gets ID
     * @return ID
     */
    public String getId() { return id; }

    /**
     * Sets ID
     * @param id ID
     */
    public void setId(String id) { this.id = id; }

    /**
     * Gets event ID
     * @return Event ID
     */
    public String getEventId() { return eventId; }

    /**
     * Sets event ID
     * @param eventId Event ID
     */
    public void setEventId(String eventId) { this.eventId = eventId; }

    /**
     * Gets the file path in Firebase
     * @return The storage path string
     */
    public String getStoragePath() { return storagePath; }

    /**
     * Sets the file path in Firebase
     * @param storagePath The new storage path string
     */
    public void setStoragePath(String storagePath) { this.storagePath = storagePath; }

    /**
     * Gets the ID of the user who uploaded the image
     * @return The uploader's ID
     */
    public String getUploaderId() { return uploaderId; }

    /**
     * Sets the ID of the user who uploaded the image.
     * @param uploaderId The new uploader's ID
     */
    public void setUploaderId(String uploaderId) { this.uploaderId = uploaderId; }

    /**
     * Gets the creation timestamp
     * @return The creation timestamp
     */
    public long getCreatedAt() { return createdAt; }

    /**
     * Sets the creation timestamp
     * @param createdAt The new creation timestamp
     */
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
