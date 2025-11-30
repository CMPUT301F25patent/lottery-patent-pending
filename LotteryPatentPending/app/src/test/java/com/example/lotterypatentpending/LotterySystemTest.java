package com.example.lotterypatentpending;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import androidx.core.util.Pair;

import com.example.lotterypatentpending.models.LotterySystem;
import com.example.lotterypatentpending.models.User;
import com.example.lotterypatentpending.models.WaitingListState;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class LotterySystemTest {
    private List<Pair<User, WaitingListState>> list;
    private User user1, user2, user3, user4, user5;

    @Before
    public void setUp() {
        list = new ArrayList<>();
        user1 = new User("test_id_1", "name_1", "email_1@test.com", "1234567890", false);
        user2 = new User("test_id_2", "name_2", "email_2@test.com", "1234567890", false);
        user3 = new User("test_id_3", "name_3", "email_3@test.com", "1234567890", false);
        user4 = new User("test_id_4", "name_4", "email_4@test.com", "1234567890", false);
        user5 = new User("test_id_5", "name_5", "email_5@test.com", "1234567890", false);

        list.add(new Pair<>(user1, WaitingListState.ENTERED));
        list.add(new Pair<>(user2, WaitingListState.ENTERED));
        list.add(new Pair<>(user3, WaitingListState.ENTERED));
        list.add(new Pair<>(user4, WaitingListState.ENTERED));
        list.add(new Pair<>(user5, WaitingListState.ENTERED));
    }

    @Test
    public void testSelectsCorrectNumberOfWinners() {
        int numSelect = 2;
        LotterySystem.lotteryDraw(list, numSelect);

        int selectedCount = 0;
        int notSelectedCount = 0;

        for (Pair<User, WaitingListState> pair : list) {
            if (pair.second == WaitingListState.SELECTED) selectedCount++;
            else if (pair.second == WaitingListState.NOT_SELECTED) notSelectedCount++;
        }

        assertEquals(numSelect, selectedCount);
        assertEquals(list.size() - numSelect, notSelectedCount);
    }

    @Test
    public void testSelectAllWhenNumGreaterThanListSize() {
        int numSelect = 10; // greater than size
        LotterySystem.lotteryDraw(list, numSelect);

        for (Pair<User, WaitingListState> pair : list) {
            assertEquals(WaitingListState.SELECTED, pair.second);
        }
    }

    @Test
    public void testEmptyListDoesNotCrash() {
        List<Pair<User, WaitingListState>> emptyList = new ArrayList<>();

        try {
            LotterySystem.lotteryDraw(emptyList, 3);
        } catch (Exception e) {
            fail("lotterySelect() should handle empty lists safely, but threw: " + e.getMessage());
        }

        assertTrue(emptyList.isEmpty());
    }

}
