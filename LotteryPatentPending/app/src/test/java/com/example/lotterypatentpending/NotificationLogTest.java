package com.example.lotterypatentpending;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.example.lotterypatentpending.models.NotificationLog;
import com.example.lotterypatentpending.models.Notification;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

public class NotificationLogTest {
    private NotificationLog log;

    @Before
    public void setUp() {
        log = new NotificationLog(
                "org_1",
                "event_9",
                Notification.Category.LOTTERY_WIN,
                Arrays.asList("u1","u2","u3"),
                "Congrats on being selected!"
        );
    }

    @Test
    public void testCtrAndGetters() {
        assertEquals("org_1", log.getOrganizerId());
        assertEquals("event_9", log.getEventId());
        assertEquals(Notification.Category.LOTTERY_WIN, log.getCategory());
        assertEquals(3, log.getRecipientIds().size());
        assertTrue(log.getPayloadPreview().startsWith("Congrats"));
    }

    @Test
    public void testSetters() {
        log.setOrganizerId("org_2");
        log.setEventId("evt_2");
        log.setCategory(Notification.Category.WAITLIST);
        log.setRecipientIds(Arrays.asList("a","b"));
        log.setPayloadPreview("preview text");

        assertEquals("org_2", log.getOrganizerId());
        assertEquals("evt_2", log.getEventId());
        assertEquals(Notification.Category.WAITLIST, log.getCategory());
        assertEquals(2, log.getRecipientIds().size());
        assertEquals("preview text", log.getPayloadPreview());
    }
}

