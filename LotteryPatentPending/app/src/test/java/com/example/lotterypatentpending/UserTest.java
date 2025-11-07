package com.example.lotterypatentpending;

import static org.junit.Assert.*;

import com.example.lotterypatentpending.models.User;
import com.example.lotterypatentpending.models.Event;

import org.junit.Test;
import java.util.List;

public class UserTest {

    @Test
    public void testUserConstructorAndGetters() {
        User u = new User("123", "Alice", "alice@example.com", "555-1234", false);

        assertEquals("123", u.getUserId());
        assertEquals("Alice", u.getName());
        assertEquals("alice@example.com", u.getEmail());
        assertEquals("555-1234", u.getContactInfo());
        assertFalse(u.isAdmin());
    }

    @Test
    public void testSetters() {
        User u = new User();
        u.setUserId("42");
        u.setName("Bob");
        u.setEmail("bob@example.com");
        u.setContactInfo("555-9999");
        u.setAdmin(true);

        assertEquals("42", u.getUserId());
        assertEquals("Bob", u.getName());
        assertEquals("bob@example.com", u.getEmail());
        assertEquals("555-9999", u.getContactInfo());
        assertTrue(u.isAdmin());
    }

    @Test
    public void testAddJoinedEvent() {
        User u = new User();
        u.addJoinedEvent("E1");
        u.addJoinedEvent("E1"); // should not duplicate

        assertEquals(1, u.getJoinedEventIds().size());
        assertTrue(u.getJoinedEventIds().contains("E1"));
    }

    @Test
    public void testRemoveJoinedEvent() {
        User u = new User();
        u.addJoinedEvent("E1");
        u.removeJoinedEvent("E1");

        assertFalse(u.getJoinedEventIds().contains("E1"));
    }

    @Test
    public void testCreateEventAddsToOrganizedEvents() {
        User u = new User("1", "Org", "org@example.com", "123", false);
        Event e = u.createEvent("Party", "Fun event", 10);

        assertEquals(1, u.getOrganizedEvents().size());
        assertEquals(e, u.getOrganizedEvents().get(0));
        assertEquals("Party", e.getTitle());
    }

    @Test
    public void testEqualsWithSameId() {
        User u1 = new User("1", "A", "a@a.com", "123", false);
        User u2 = new User("1", "B", "b@b.com", "456", true);

        assertEquals(u1, u2); // same ID → considered equal
    }

    @Test
    public void testEqualsWithDifferentId() {
        User u1 = new User("1", "A", "a@a.com", "123", false);
        User u2 = new User("2", "B", "b@b.com", "456", false);

        assertNotEquals(u1, u2);
    }

    @Test
    public void testEqualsWithNullIds() {
        User u1 = new User();
        User u2 = new User();

        assertEquals(u1, u2); // matches your current equals() logic
    }

    @Test
    public void testHashCodeMatchesId() {
        User u = new User("ABC", "Test", "t@test.com", "000", false);
        assertEquals("ABC".hashCode(), u.hashCode());
    }
}
