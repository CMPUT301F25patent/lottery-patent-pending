package com.example.lotterypatentpending;

import static org.junit.Assert.*;

import com.example.lotterypatentpending.models.Event;
import com.example.lotterypatentpending.models.EventState;
import com.example.lotterypatentpending.models.User;
import com.example.lotterypatentpending.models.WaitingListState;
import com.google.firebase.Timestamp;
import androidx.core.util.Pair;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Unit tests for the Event class (Continuation).
 */
public class EventModelTest {

    private User organizer;
    private Event event;
    private User u1, u2, u3, u4, u5;

    @Before
    public void setup() {
        organizer = new User("uid123", "Test Organizer", "email@email.com", "5789223354");
        // Capacity of 3 for easier testing of counts
        event = new Event("Test Event", "Description", 3, organizer);

        // Setup mock users
        u1 = new User("1", "User A", "a@a.com", "111");
        u2 = new User("2", "User B", "b@b.com", "222");
        u3 = new User("3", "User C", "c@c.com", "333");
        u4 = new User("4", "User D", "d@d.com", "444");
        u5 = new User("5", "User E", "e@e.com", "555");
    }

    // ---------- Constructor & Basic Getters/Setters ----------
    @Test
    public void testConstructorInitializesFields() {
        assertNotNull(event.getId());
        assertEquals("Test Event", event.getTitle());
        assertEquals(3, event.getCapacity());
        assertEquals(EventState.NOT_STARTED, event.getEventState());
    }

    // ---------- Tag Setter ----------
    @Test
    public void testSetTag_FormatsCorrectly() {
        event.setTag("tech");
        assertEquals("Tech", event.getTag());

        event.setTag("sPoRTs");
        assertEquals("Sports", event.getTag());

        event.setTag("null");
        assertEquals("Null", event.getTag());
    }

    @Test
    public void testSetTag_HandlesNullOrEmpty() {
        event.setTag(null);
        assertEquals("General", event.getTag());

        event.setTag("  ");
        assertEquals("General", event.getTag());
    }

    // ---------- Waiting List Capacity Setter ----------
    @Test
    public void testSetWaitingListCapacity_UpdatesBothEventAndWaitingList() {
        event.setWaitingListCapacity(5);
        assertEquals(5, event.getWaitingListCapacity());
        assertEquals(5, event.getWaitingList().getCapacity());
    }

    // ---------- Waiting List / User State Helpers ----------

    private void addUsersWithStates(Pair<User, WaitingListState>... entries) {
        // Since Event.joinEvent only adds with ENTERED state, we must
        // manually set up the list to test other states.
        ArrayList<Pair<User, WaitingListState>> list = new ArrayList<>();
        for (Pair<User, WaitingListState> entry : entries) {
            list.add(entry);
        }
        event.getWaitingList().setList(list);
    }

    @Test
    public void testGetWaitingListStateForUser() {
        addUsersWithStates(
                new Pair<>(u1, WaitingListState.ENTERED),
                new Pair<>(u2, WaitingListState.SELECTED),
                new Pair<>(u3, WaitingListState.ACCEPTED)
        );

        assertEquals(WaitingListState.ENTERED, event.getWaitingListStateForUser(u1));
        assertEquals(WaitingListState.SELECTED, event.getWaitingListStateForUser(u2));
        assertEquals(WaitingListState.ACCEPTED, event.getWaitingListStateForUser(u3));
        assertEquals(WaitingListState.NOT_IN, event.getWaitingListStateForUser(u4));
        assertEquals(WaitingListState.NOT_IN, event.getWaitingListStateForUser(null));
    }

    @Test
    public void testUpdateEntrantState() {
        event.joinEvent(u1);
        event.joinEvent(u2);

        // Update u1's state
        assertTrue(event.updateEntrantState(u1, WaitingListState.SELECTED));
        assertEquals(WaitingListState.SELECTED, event.getWaitingListStateForUser(u1));

        // Try updating a user not in the list
        assertFalse(event.updateEntrantState(u3, WaitingListState.ACCEPTED));
        assertEquals(WaitingListState.ENTERED, event.getWaitingListStateForUser(u2));
    }

    // ---------- Entrant/Spot Counting ----------

    @Test
    public void testGetAcceptedCount() {
        addUsersWithStates(
                new Pair<>(u1, WaitingListState.ACCEPTED),
                new Pair<>(u2, WaitingListState.SELECTED),
                new Pair<>(u3, WaitingListState.CANCELED),
                new Pair<>(u4, WaitingListState.ACCEPTED)
        );

        assertEquals(2, event.getAcceptedCount());
    }

    @Test
    public void testGetTakenSpotsCount() {
        // Taken spots = SELECTED + ACCEPTED
        addUsersWithStates(
                new Pair<>(u1, WaitingListState.ACCEPTED),
                new Pair<>(u2, WaitingListState.SELECTED),
                new Pair<>(u3, WaitingListState.CANCELED),
                new Pair<>(u4, WaitingListState.ENTERED),
                new Pair<>(u5, WaitingListState.ACCEPTED)
        );

        // u1(ACCEPTED) + u2(SELECTED) + u5(ACCEPTED) = 3
        assertEquals(3, event.getTakenSpotsCount());
    }

    @Test
    public void testGetRemainingCapacity() {
        // Capacity is 3

        // 0 taken spots 3 remaining
        assertEquals(3, event.getRemainingCapacity());

        // Add one ACCEPTED user 1 taken spot
        addUsersWithStates(new Pair<>(u1, WaitingListState.ACCEPTED));
        assertEquals(2, event.getRemainingCapacity());

        // Add two more taken spots (SELECTED, ACCEPTED) 3 taken spots
        addUsersWithStates(
                new Pair<>(u1, WaitingListState.ACCEPTED),
                new Pair<>(u2, WaitingListState.SELECTED),
                new Pair<>(u3, WaitingListState.ACCEPTED)
        );
        assertEquals(0, event.getRemainingCapacity());

        // Add one more taken spot (over capacity) Still 0 remaining
        addUsersWithStates(
                new Pair<>(u1, WaitingListState.ACCEPTED),
                new Pair<>(u2, WaitingListState.SELECTED),
                new Pair<>(u3, WaitingListState.ACCEPTED),
                new Pair<>(u4, WaitingListState.SELECTED)
        );
        assertEquals(0, event.getRemainingCapacity());
    }

    // ---------- Event State Transitions ----------

    @Test
    public void testConfirmEntrants() {
        event.setEventState(EventState.SELECTED_ENTRANTS);
        event.confirmEntrants();
        assertEquals(EventState.CONFIRMED_ENTRANTS, event.getEventState());
    }

    @Test
    public void testEndEvent() {
        event.setEventState(EventState.CONFIRMED_ENTRANTS);
        event.endEvent();
        assertEquals(EventState.ENDED, event.getEventState());
    }

    @Test
    public void testCancelEvent() {
        event.setEventState(EventState.OPEN_FOR_REG);
        event.cancelEvent();
        assertEquals(EventState.CANCELLED, event.getEventState());
    }

    // ---------- Formatted Registration Window ----------

    @Test
    public void testGetFormattedRegWindow_NoDates() {
        event.setRegStartDate(null);
        event.setRegEndDate(null);
        assertEquals("Not set", event.getFormattedRegWindow());
    }

    @Test
    public void testGetFormattedRegWindow_WithDates() {
        long fixedTime = 1767283200000L;
        event.setRegStartDate(new Timestamp(new Date(fixedTime)));
        event.setRegEndDate(new Timestamp(new Date(fixedTime + 3600000))); // 1 hour later

        String formattedWindow = event.getFormattedRegWindow();

        assertTrue(formattedWindow.contains(" – "));

        assertNotEquals("Not set", formattedWindow);

        event.setRegStartDate(null);
        assertTrue(event.getFormattedRegWindow().contains("N/A"));

        event.setRegStartDate(new Timestamp(new Date(fixedTime)));
        event.setRegEndDate(null);
        assertTrue(event.getFormattedRegWindow().contains("N/A"));
    }
}