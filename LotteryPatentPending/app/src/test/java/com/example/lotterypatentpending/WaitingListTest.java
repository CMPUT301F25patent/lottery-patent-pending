package com.example.lotterypatentpending;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.example.lotterypatentpending.exceptions.UserInListException;
import com.example.lotterypatentpending.exceptions.UserNotInListException;
import com.example.lotterypatentpending.models.LotterySystem;
import com.example.lotterypatentpending.models.User;
import com.example.lotterypatentpending.models.WaitingList;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

public class WaitingListTest {
    private WaitingList waitingList;
    private User user1;
    private User user2;

    @Before
    public void setUp() {
        waitingList = new WaitingList();
        user1 = new User("test_id_1", "name_1", "email_1@test.com", "1234567890", false);
        user2 = new User("test_id_2", "name_2", "email_2@test.com", "1234567890", false);
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

        // Just make sure the method runs
        try {
            waitingList.lotterySelect(1);
        } catch (Exception e) {
            fail("lotterySelect() should not throw an exception: " + e.getMessage());
        }
    }
}
