package com.example.lotterypatentpending;

import static org.junit.Assert.*;

import com.example.lotterypatentpending.models.ImageRecord;

import org.junit.Before;
import org.junit.Test;

public class ImageRecordTest {

    private static final String TEST_ID = "record_001";
    private static final String TEST_EVENT_ID = "event_xyz";
    private static final String TEST_STORAGE_PATH = "event_posters/event_xyz_v1.jpg";
    private static final String TEST_UPLOADER_ID = "user_organizer";
    private static final long TEST_CREATED_AT = 1678886400000L; // March 15, 2023, 12:00:00 AM UTC

    private ImageRecord imageRecord;

    @Before
    public void setUp() {
        imageRecord = new ImageRecord(
                TEST_ID,
                TEST_EVENT_ID,
                TEST_STORAGE_PATH,
                TEST_UPLOADER_ID,
                TEST_CREATED_AT
        );
    }

    // --- Constructor Test ---
    @Test
    public void testFullConstructorAndGetters() {
        assertEquals(TEST_ID, imageRecord.getId());
        assertEquals(TEST_EVENT_ID, imageRecord.getEventId());
        assertEquals(TEST_STORAGE_PATH, imageRecord.getStoragePath());
        assertEquals(TEST_UPLOADER_ID, imageRecord.getUploaderId());
        assertEquals(TEST_CREATED_AT, imageRecord.getCreatedAt());
    }

    // --- Empty Constructor Test (for Firestore) ---
    @Test
    public void testEmptyConstructor() {
        ImageRecord emptyRecord = new ImageRecord();
        assertNull(emptyRecord.getId());
        assertNull(emptyRecord.getEventId());
        assertNull(emptyRecord.getStoragePath());
        assertNull(emptyRecord.getUploaderId());
        assertEquals(0L, emptyRecord.getCreatedAt());
    }

    // --- Setter Tests ---

    @Test
    public void testSetId() {
        String newId = "record_002";
        imageRecord.setId(newId);
        assertEquals(newId, imageRecord.getId());
    }

    @Test
    public void testSetEventId() {
        String newEventId = "event_abc";
        imageRecord.setEventId(newEventId);
        assertEquals(newEventId, imageRecord.getEventId());
    }

    @Test
    public void testSetStoragePath() {
        String newPath = "event_posters/new_path.png";
        imageRecord.setStoragePath(newPath);
        assertEquals(newPath, imageRecord.getStoragePath());
    }

    @Test
    public void testSetUploaderId() {
        String newUploaderId = "user_contributor";
        imageRecord.setUploaderId(newUploaderId);
        assertEquals(newUploaderId, imageRecord.getUploaderId());
    }

    @Test
    public void testSetCreatedAt() {
        long newTimestamp = 1678972800000L; // March 16, 2023
        imageRecord.setCreatedAt(newTimestamp);
        assertEquals(newTimestamp, imageRecord.getCreatedAt());
    }

    @Test
    public void testSettersWithNullValues() {
        imageRecord.setUploaderId(null);
        assertNull(imageRecord.getUploaderId());

        imageRecord.setStoragePath(null);
        assertNull(imageRecord.getStoragePath());
    }
}