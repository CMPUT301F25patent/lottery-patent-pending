package com.example.lotterypatentpending;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import androidx.core.util.Pair;

import com.example.lotterypatentpending.exceptions.UserInListException;
import com.example.lotterypatentpending.exceptions.UserNotInListException;
import com.example.lotterypatentpending.models.LotterySystem;
import com.example.lotterypatentpending.models.User;
import com.example.lotterypatentpending.models.WaitingList;
import com.example.lotterypatentpending.models.WaitingListState;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class WaitingListTest {
    private WaitingList waitingList;
    private User user1;
    private User user2;
    private User user3;

    @Before
    public void setUp() {
        waitingList = new WaitingList();
        user1 = new User("test_id_1", "name_1", "email_1@test.com", "1234567890", false);
        user2 = new User("test_id_2", "name_2", "email_2@test.com", "2222222222", false);
        user3 = new User("test_id_3", "name_3", "email_3@test.com", "3333333333", false);
    }

    @Test
    public void testAddEntrantSuccessfully() {
        waitingList.addEntrant(user1);
        assertTrue(waitingList.checkEntrant(user1));
        assertEquals(Integer.valueOf(1), waitingList.getNumEntrants());
    }

    @Test(expected = UserInListException.class)
    public void testAddEntrantThrowsIfAlreadyExists() {
        waitingList.addEntrant(user1);
        waitingList.addEntrant(user1); // should throw
    }

    @Test
    public void testRemoveEntrantSuccessfully() {
        waitingList.addEntrant(user1);
        waitingList.removeEntrant(user1);
        assertFalse(waitingList.checkEntrant(user1));
        assertEquals(Integer.valueOf(0), waitingList.getNumEntrants());
    }

    @Test(expected = UserNotInListException.class)
    public void testRemoveEntrantThrowsIfNotExists() {
        waitingList.removeEntrant(user1); // should throw
    }

    @Test
    public void testCheckEntrant() {
        waitingList.addEntrant(user1);
        assertTrue(waitingList.checkEntrant(user1));
        assertFalse(waitingList.checkEntrant(user2));
    }

    @Test
    public void testGetNumEntrants() {
        waitingList.addEntrant(user1);
        waitingList.addEntrant(user2);
        assertEquals(Integer.valueOf(2), waitingList.getNumEntrants());
    }

    @Test
    public void testLotterySelectDoesNotCrash() {
        waitingList.addEntrant(user1);
        waitingList.addEntrant(user2);

        try {
            waitingList.lotterySelect(1);
            int selectedCount = 0;
            int notSelectedCount = 0;
            for (Pair<User, WaitingListState> pair : waitingList.getList()) {
                if (pair.second == WaitingListState.SELECTED) selectedCount++;
                else if (pair.second == WaitingListState.NOT_SELECTED) notSelectedCount++;
            }
            assertEquals(1, selectedCount);
            assertEquals(1, notSelectedCount);
        } catch (Exception e) {
            fail("lotterySelect() should not throw an exception: " + e.getMessage());
        }
    }

    @Test
    public void testCapacityConstructorAndGetterSetter() {
        WaitingList cappedList = new WaitingList(5);
        assertEquals(5, cappedList.getCapacity());

        cappedList.setCapacity(10);
        assertEquals(10, cappedList.getCapacity());

        // Test default capacity
        assertEquals(-1, waitingList.getCapacity());
    }

    @Test
    public void testAddEntrantRespectsCapacity() {
        WaitingList cappedList = new WaitingList(1);
        cappedList.addEntrant(user1);

        try {
            cappedList.addEntrant(user2);
        } catch (Exception e) {
            fail("Should return gracefully if capacity is reached, not throw.");
        }

        assertEquals(1, cappedList.getNumEntrants().intValue());
        assertFalse(cappedList.checkEntrant(user2));
    }

    @Test
    public void testUpdateEntrantStateSuccessfully() {
        waitingList.addEntrant(user1); // initial state: ENTERED

        boolean updated = waitingList.updateEntrantState(user1, WaitingListState.ACCEPTED);
        assertTrue(updated);

        Pair<User, WaitingListState> pair = waitingList.getList().get(0);
        assertEquals(WaitingListState.ACCEPTED, pair.second);
        assertEquals(user1, pair.first);
    }

    @Test
    public void testUpdateEntrantStateUserNotFound() {
        waitingList.addEntrant(user1);

        boolean updated = waitingList.updateEntrantState(user2, WaitingListState.ACCEPTED);
        assertFalse(updated);

        assertEquals(WaitingListState.ENTERED, waitingList.getList().get(0).second);
    }

    @Test
    public void testGetSelectedEntrants() {
        ArrayList<Pair<User, WaitingListState>> list = new ArrayList<>();
        list.add(new Pair<>(user1, WaitingListState.SELECTED));
        list.add(new Pair<>(user2, WaitingListState.ACCEPTED));
        list.add(new Pair<>(user3, WaitingListState.NOT_SELECTED));
        waitingList.setList(list);

        List<User> selected = waitingList.getSelectedEntrants();

        assertEquals(1, selected.size());
        assertTrue(selected.contains(user1));
        assertFalse(selected.contains(user2));
    }

    @Test
    public void testGetUsersOnly() {
        waitingList.addEntrant(user1);
        waitingList.addEntrant(user2);

        List<User> users = waitingList.getUsersOnly();

        assertEquals(2, users.size());
        assertTrue(users.contains(user1));
        assertTrue(users.contains(user2));
    }

    @Test
    public void testExportAcceptedEntrantsToCsv_NoAccepted() {
        waitingList.addEntrant(user1);

        String csv = waitingList.exportAcceptedEntrantsToCsv();

        assertEquals("Name,Email,Phone\n", csv);
    }

    @Test
    public void testExportAcceptedEntrantsToCsv_OneAccepted() {
        waitingList.addEntrant(user1);
        waitingList.updateEntrantState(user1, WaitingListState.ACCEPTED);

        String expectedCsv = "Name,Email,Phone\n" +
                "name_1,email_1@test.com,1234567890,\n";

        assertEquals(expectedCsv, waitingList.exportAcceptedEntrantsToCsv());
    }

    @Test
    public void testExportAcceptedEntrantsToCsv_MultipleAcceptedAndOthers() {
        // u1: ACCEPTED, u2: SELECTED, u3: ACCEPTED
        waitingList.addEntrant(user1);
        waitingList.addEntrant(user2);
        waitingList.addEntrant(user3);

        waitingList.updateEntrantState(user1, WaitingListState.ACCEPTED);
        waitingList.updateEntrantState(user2, WaitingListState.SELECTED);
        waitingList.updateEntrantState(user3, WaitingListState.ACCEPTED);

        String expectedCsv = "Name,Email,Phone\n" +
                "name_1,email_1@test.com,1234567890,\n" +
                "name_3,email_3@test.com,3333333333,\n";

        String actualCsv = waitingList.exportAcceptedEntrantsToCsv();

        assertTrue(actualCsv.startsWith("Name,Email,Phone\n"));
        assertTrue(actualCsv.contains("name_1,email_1@test.com,1234567890"));
        assertTrue(actualCsv.contains("name_3,email_3@test.com,3333333333"));
        assertFalse(actualCsv.contains("name_2"));
    }

    @Test
    public void testExportAcceptedEntrantsToCsv_HandlesNullsInUserFields() {
        User userNulls = new User("uid", null, null, null, false);
        waitingList.addEntrant(userNulls);
        waitingList.updateEntrantState(userNulls, WaitingListState.ACCEPTED);

        String expectedCsv = "Name,Email,Phone\n" +
                ",,,\n";

        assertEquals(expectedCsv, waitingList.exportAcceptedEntrantsToCsv());
    }
}