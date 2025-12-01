package com.example.lotterypatentpending;

import static org.junit.Assert.*;

import com.example.lotterypatentpending.models.User;
import com.example.lotterypatentpending.models.UserLocation;

import org.junit.Test;
import org.junit.Before;
import java.util.Arrays;
import java.util.List;

public class UserTest {

    private User u;
    private static final String ID = "123";
    private static final String NAME = "Alice";
    private static final String EMAIL = "alice@example.com";
    private static final String CONTACT = "555-1234";

    @Before
    public void setup() {
        // Use the full constructor for a fresh start in most tests
        u = new User(ID, NAME, EMAIL, CONTACT, false);
    }

    // --- Constructor Tests ---
    @Test
    public void testUserFullConstructorAndGetters() {
        User adminUser = new User(ID, NAME, EMAIL, CONTACT, true);

        assertEquals(ID, adminUser.getUserId());
        assertEquals(NAME, adminUser.getName());
        assertEquals(EMAIL, adminUser.getEmail());
        assertEquals(CONTACT, adminUser.getContactInfo());
        assertTrue(adminUser.isAdmin());
        assertTrue(adminUser.isNotificationsOptIn()); // Default is true
    }

    @Test
    public void testUserNonAdminConstructor() {
        assertEquals(ID, u.getUserId());
        assertFalse(u.isAdmin());
    }

    @Test
    public void testDefaultConstructorInitializesLists() {
        User defaultUser = new User();
        assertNotNull(defaultUser.getJoinedEventIds());
        assertNotNull(defaultUser.getAcceptedEventIds());
        assertNotNull(defaultUser.getDeclinedEventIds());
        assertNotNull(defaultUser.getPastEventIds());
        assertTrue(defaultUser.isNotificationsOptIn());
    }

    // --- Basic Getters and Setters (Re-check) ---
    @Test
    public void testSetters() {
        u.setUserId("42");
        u.setName("Bob");
        u.setEmail("bob@example.com");
        u.setContactInfo("555-9999");
        u.setAdmin(true);
        u.setNotificationsOptIn(false);

        assertEquals("42", u.getUserId());
        assertEquals("Bob", u.getName());
        assertEquals("bob@example.com", u.getEmail());
        assertEquals("555-9999", u.getContactInfo());
        assertTrue(u.isAdmin());
        assertFalse(u.isNotificationsOptIn());
    }

    @Test
    public void testLocationMethods() {
        u.addLocation(53.54, -113.49); // Edmonton

        UserLocation location = u.getLocation();
        assertNotNull(location);

        // Use delta for double comparison
        assertEquals(53.54, location.getLat(), 0.0001);
        assertEquals(-113.49, location.getLng(), 0.0001);

        // Test setLocation
        UserLocation newLocation = new UserLocation(40.71, -74.00); // NYC
        u.setLocation(newLocation);
        assertEquals(newLocation, u.getLocation());
    }


    @Test
    public void testAddJoinedEvent() {
        u.addJoinedEvent("E1");
        u.addJoinedEvent("E1"); // should not duplicate

        assertEquals(1, u.getJoinedEventIds().size());
        assertTrue(u.getJoinedEventIds().contains("E1"));
    }

    @Test
    public void testRemoveJoinedEvent() {
        u.addJoinedEvent("E1");
        u.addJoinedEvent("E2");
        u.removeJoinedEvent("E1");
        u.removeJoinedEvent("E3"); // attempt to remove non-existent

        assertEquals(1, u.getJoinedEventIds().size());
        assertFalse(u.getJoinedEventIds().contains("E1"));
        assertTrue(u.getJoinedEventIds().contains("E2"));
    }

    @Test
    public void testAddAndRemoveAcceptedEvent() {
        u.addAcceptedEvent("A1");
        u.addAcceptedEvent("A2");
        u.addAcceptedEvent("A1"); // no duplicate

        assertEquals(2, u.getAcceptedEventIds().size());
        assertTrue(u.getAcceptedEventIds().contains("A1"));

        u.removeAcceptedEvent("A1");
        assertFalse(u.getAcceptedEventIds().contains("A1"));
        assertEquals(1, u.getAcceptedEventIds().size());
    }

    @Test
    public void testAddAndRemoveDeclinedEvent() {
        u.addDeclinedEvent("D1");
        u.addDeclinedEvent("D2");

        assertEquals(2, u.getDeclinedEventIds().size());
        assertTrue(u.getDeclinedEventIds().contains("D1"));

        u.removeDeclinedEvent("D1");
        assertFalse(u.getDeclinedEventIds().contains("D1"));
        assertEquals(1, u.getDeclinedEventIds().size());
    }

    @Test
    public void testAddAndRemovePastEvent() {
        u.addPastEvent("P1");
        u.addPastEvent("P2");

        assertEquals(2, u.getPastEventIds().size());
        assertTrue(u.getPastEventIds().contains("P1"));

        u.removePastEvent("P1");
        assertFalse(u.getPastEventIds().contains("P1"));
        assertEquals(1, u.getPastEventIds().size());
    }

    @Test
    public void testSetListMethods() {
        List<String> newJoined = Arrays.asList("J1", "J2");
        u.setJoinedEventIds(newJoined);
        assertEquals(2, u.getJoinedEventIds().size());
        assertTrue(u.getJoinedEventIds().contains("J1"));
    }

    @Test
    public void testEqualsWithSameId() {
        User u1 = new User("1", "A", "a@a.com", "123", false);
        User u2 = new User("1", "B", "b@b.com", "456", true);

        assertEquals(u1, u2); // same ID → considered equal
    }

    @Test
    public void testEqualsWithNullIds() {
        User u1 = new User(); // userId = null
        User u2 = new User(); // userId = null

        assertEquals(u1, u2); // both null IDs should be equal per current implementation
    }

    @Test
    public void testEqualsWithOneNullId() {
        User u1 = new User("1", "A", "a@a.com", "123", false);
        User u2 = new User(); // userId = null

        assertNotEquals(u1, u2);
    }

    @Test
    public void testHashCodeMatchesId() {
        User u = new User("ABC", "Test", "t@test.com", "000", false);
        assertEquals("ABC".hashCode(), u.hashCode());
    }

    @Test
    public void testHashCodeWithNullId() {
        User u = new User();
        assertEquals(0, u.hashCode());
    }

    @Test
    public void testToStringContainsAllFields() {
        u.addJoinedEvent("X");
        u.addAcceptedEvent("Y");
        u.setAdmin(true);
        u.setNotificationsOptIn(false);

        String result = u.toString();

        assertTrue(result.contains("userId='123'"));
        assertTrue(result.contains("name='Alice'"));
        assertTrue(result.contains("email='alice@example.com'"));
        assertTrue(result.contains("joinedEventIds=[X]"));
        assertTrue(result.contains("acceptedEventIds=[Y]"));
        assertTrue(result.contains("notificationsOptIn=false"));
        assertTrue(result.contains("isAdmin=true"));

        // Should not contain UserLocation details
        assertFalse(result.contains("location"));
    }
}