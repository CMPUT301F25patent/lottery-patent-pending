package com.example.lotterypatentpending;

import static org.junit.Assert.*;

import com.example.lotterypatentpending.models.Event;
import com.example.lotterypatentpending.models.User;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for AdminEventsActivity logic, using dummy event data.
 * These tests verify deletion logic and event list management.
 */
public class AdminEventActivityTest {

    private List<Event> eventList;
    private Event event1;
    private Event event2;
    private User organizer;

    @Before
    public void setUp() {
        organizer = new User("org001", "Event Organizer", "org@email.com", "555-7777", false);
        eventList = new ArrayList<>();

        event1 = new Event("Hackathon", "Tech coding event", 100, organizer);
        event2 = new Event("Music Fest", "Outdoor concert", 300, organizer);

        eventList.add(event1);
        eventList.add(event2);
    }

    @Test
    public void testInitialEventListHasTwoEvents() {
        assertEquals(2, eventList.size());
    }

    @Test
    public void testRemoveEventById() {
        String removeId = event1.getId();
        boolean removed = eventList.removeIf(e -> e.getId().equals(removeId));
        assertTrue(removed);
        assertEquals(1, eventList.size());
        assertFalse(eventList.contains(event1));
    }

    @Test
    public void testRemoveEventThatDoesNotExist() {
        boolean removed = eventList.removeIf(e -> e.getId().equals("fakeId123"));
        assertFalse(removed);
        assertEquals(2, eventList.size());
    }

    @Test
    public void testEventOrganizerInfoIsCorrect() {
        assertEquals("Event Organizer", event1.getOrganizer().getName());
        assertEquals("org@email.com", event1.getOrganizer().getEmail());
    }

    @Test
    public void testEventTitlesAreSetCorrectly() {
        assertEquals("Hackathon", event1.getTitle());
        assertEquals("Music Fest", event2.getTitle());
    }
}
