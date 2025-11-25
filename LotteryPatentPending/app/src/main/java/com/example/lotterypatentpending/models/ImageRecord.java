package com.example.lotterypatentpending.models;

// package com.example.lotterypatentpending.models;

public class ImageRecord {
    private String id;          // Firestore doc id
    private String eventId;     // linked event
    private String storagePath; // path in Firebase Storage (e.g. "event_posters/<eventId>.jpg")
    private String uploaderId;  // organizer uid (optional)
    private long createdAt;     // millis since epoch

    public ImageRecord() {}

    public ImageRecord(String id, String eventId, String storagePath, String uploaderId, long createdAt) {
        this.id = id;
        this.eventId = eventId;
        this.storagePath = storagePath;
        this.uploaderId = uploaderId;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getStoragePath() { return storagePath; }
    public void setStoragePath(String storagePath) { this.storagePath = storagePath; }

    public String getUploaderId() { return uploaderId; }
    public void setUploaderId(String uploaderId) { this.uploaderId = uploaderId; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
