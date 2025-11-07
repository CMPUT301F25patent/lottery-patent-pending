package com.example.lotterypatentpending;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import com.example.lotterypatentpending.models.Notification;

import org.junit.Before;
import org.junit.Test;

public class NotificationTest {
    private Notification n;

    @Before
    public void setUp() {
        // userId, eventId, organizerId, title, body, category
        n = new Notification("user_1", "event_1", "org_1",
                "Welcome", "Body text", Notification.Category.WAITLIST);
        n.setId("doc_1");
    }

    @Test
    public void testConstructorAndDefaults() {
        assertEquals("doc_1", n.getId());
        assertEquals("user_1", n.getUserId());
        assertEquals("event_1", n.getEventId());
        assertEquals("org_1", n.getSenderId());
        assertEquals("Welcome", n.getTitle());
        assertEquals("Body text", n.getBody());
        assertEquals(Notification.Category.WAITLIST, n.getCategory());
        assertFalse(n.isRead()); // default should be false
    }

    @Test
    public void testSetters() {
        n.setUserId("user_2");
        n.setEventId("event_2");
        n.setSenderId("org_2");
        n.setTitle("New Title");
        n.setBody("New Body");
        n.setCategory(Notification.Category.SELECTED);
        n.setRead(true);
        n.setStatus("SENT");

        assertEquals("user_2", n.getUserId());
        assertEquals("event_2", n.getEventId());
        assertEquals("org_2", n.getSenderId());
        assertEquals("New Title", n.getTitle());
        assertEquals("New Body", n.getBody());
        assertEquals(Notification.Category.SELECTED, n.getCategory());
        assertTrue(n.isRead());
        assertEquals("SENT", n.getStatus());
    }

    @Test
    public void testEqualsAndHashCodeBasedOnId() {
        Notification a = new Notification("u","e","o","t","b", Notification.Category.WIN);
        Notification b = new Notification("u","e","o","t","b", Notification.Category.WIN);
        a.setId("X");
        b.setId("X");

        Notification c = new Notification("u","e","o","t","b", Notification.Category.WIN);
        c.setId("Y");

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
    }
}

