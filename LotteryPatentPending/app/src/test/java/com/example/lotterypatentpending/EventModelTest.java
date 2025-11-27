package com.example.lotterypatentpending;

import static org.junit.jupiter.api.Assertions.*;

import com.example.lotterypatentpending.models.Event;
import com.example.lotterypatentpending.models.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the Event class.
 */
public class EventModelTest {

    private User organizer;
    private Event event;

    @BeforeEach
    void setup() {
        organizer = new User("uid123", "Test Organizer", "email@email.com", "5789223354");
        event = new Event("Test Event", "Description", 10, organizer);
    }

    // ---------- Constructor ----------
    @Test
    void testConstructorInitializesFields() {
        assertNotNull(event.getId());
        assertEquals("Test Event", event.getTitle());
        assertEquals("Description", event.getDescription());
        assertEquals(10, event.getCapacity());
        assertEquals(organizer, event.getOrganizer());
        assertNotNull(event.getWaitingList());
        assertEquals(-1, event.getWaitingListCapacity());
        assertFalse(event.isGeolocationRequired());
    }

    // ---------- Getters & Setters ----------
    @Test
    void testSettersAndGetters() {
        event.setTitle("Updated Title");
        event.setDescription("Updated Desc");
        event.setCapacity(20);
        event.setLocation("New York");

        assertEquals("Updated Title", event.getTitle());
        assertEquals("Updated Desc", event.getDescription());
        assertEquals(20, event.getCapacity());
        assertEquals("New York", event.getLocation());
    }

    // ---------- Waiting List ----------
    @Test
    void testAddToWaitingList() {
        User u = new User("id1", "User 1", "u1@example.com", "556");
        event.addToWaitingList(u);

        assertTrue(event.inWaitingList(u));
    }

    @Test
    void testRemoveFromWaitingList() {
        User u = new User("id1", "User 1", "u1@example.com", "5222");
        event.addToWaitingList(u);
        event.removeFromWaitingList(u);

        assertFalse(event.inWaitingList(u));
    }

    @Test
    void testJoinEventUnlimitedWaitingList() {
        event.setWaitingListCapacity(-1); // unlimited
        User u = new User("id1", "User 1", "u1@example.com", "57921");

        event.joinEvent(u);

        assertTrue(event.inWaitingList(u));
    }

    @Test
    void testJoinEventRespectsCapacity() {
        event.setWaitingListCapacity(1);

        User u1 = new User("1", "A", "a@a.com", "432233");
        User u2 = new User("2", "B", "b@b.com", "4324234");

        event.joinEvent(u1);
        event.joinEvent(u2);   // should fail due to capacity 1

        assertTrue(event.inWaitingList(u1));
        assertFalse(event.inWaitingList(u2));
    }

    @Test
    void testJoinEventDoesNotDuplicateUsers() {
        event.setWaitingListCapacity(5);
        User u = new User("id1", "User 1", "u1@example.com", "fsffdsfd");

        event.joinEvent(u);
        event.joinEvent(u); // second time should do nothing

        assertEquals(1, event.getWaitingList().getList().size());
    }

    // ---------- Geo Location ----------
    @Test
    void testGeolocationRequiredFlag() {
        assertFalse(event.isGeolocationRequired());

        event.setGeolocationRequired(true);

        assertTrue(event.isGeolocationRequired());
    }

    // ---------- Activity State ----------
    @Test
    void testIsActive_NoDates() {
        event.setRegStartDate(null);
        event.setRegEndDate(null);

        assertFalse(event.isOpenForReg());
    }

//    @Test
//    void testIsActive_WithinRange() {
//        LocalDateTime now = LocalDateTime.now();
//        event.setRegStartDate(now.minusDays(1));
//        event.setRegEndDate(now.plusDays(1));
//
//        assertTrue(event.isActive());
//    }
//
//    @Test
//    void testIsActive_AfterEndDate() {
//        LocalDateTime now = LocalDateTime.now();
//        event.setRegStartDate(now.minusDays(3));
//        event.setRegEndDate(now.minusDays(1));
//
//        assertFalse(event.isActive());
//    }
//
//    @Test
//    void testIsActive_BeforeStartDate() {
//        LocalDateTime now = LocalDateTime.now();
//        event.setRegStartDate(now.plusDays(1));
//        event.setRegEndDate(now.plusDays(3));
//
//        assertFalse(event.isActive());
//    }
//
//    @Test
//    void testIsActive_OnlyStartDate() {
//        LocalDateTime now = LocalDateTime.now();
//        event.setRegStartDate(now.minusHours(1));
//        event.setRegEndDate(null);
//
//        assertTrue(event.isActive());
//    }
//
//    @Test
//    void testIsActive_OnlyEndDate() {
//        LocalDateTime now = LocalDateTime.now();
//        event.setRegStartDate(null);
//        event.setRegEndDate(now.plusHours(1));
//
//        assertTrue(event.isActive());
//    }
}
