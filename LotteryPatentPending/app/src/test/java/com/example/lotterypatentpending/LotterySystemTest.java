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
import java.util.Collections;
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

    @Test
    public void testDrawFromEnteredAndNotSelectedCandidates() {
        list.clear();
        list.add(new Pair<>(user1, WaitingListState.ACCEPTED));
        list.add(new Pair<>(user2, WaitingListState.ENTERED));
        list.add(new Pair<>(user3, WaitingListState.NOT_SELECTED));
        list.add(new Pair<>(user4, WaitingListState.DECLINED));
        list.add(new Pair<>(user5, WaitingListState.ENTERED));

        int numSelect = 1;
        LotterySystem.lotteryDraw(list, numSelect);

        assertEquals(WaitingListState.ACCEPTED, list.get(0).second);
        assertEquals(WaitingListState.DECLINED, list.get(3).second);

        int selectedCount = 0;
        int notSelectedCount = 0;
        for (Pair<User, WaitingListState> pair : list) {
            if (pair.second == WaitingListState.SELECTED) selectedCount++;
            else if (pair.second == WaitingListState.NOT_SELECTED) notSelectedCount++;
        }

        assertEquals(1, selectedCount);
        assertEquals(2, notSelectedCount);

        assertEquals(5, list.size());
    }

    @Test
    public void testSelectZeroSlots() {
        int numSelect = 0;
        LotterySystem.lotteryDraw(list, numSelect);

        for (Pair<User, WaitingListState> pair : list) {
            assertEquals(WaitingListState.NOT_SELECTED, pair.second);
        }
    }

    @Test
    public void testNegativeSlotsDoesNotCrashAndSelectsNone() {
        int numSelect = -5;
        LotterySystem.lotteryDraw(list, numSelect);

        int selectedCount = 0;
        int notSelectedCount = 0;

        for (Pair<User, WaitingListState> pair : list) {
            if (pair.second == WaitingListState.SELECTED) selectedCount++;
            else if (pair.second == WaitingListState.NOT_SELECTED) notSelectedCount++;
        }

        assertEquals(0, selectedCount + notSelectedCount);

        for (Pair<User, WaitingListState> pair : list) {
            assertEquals(WaitingListState.ENTERED, pair.second);
        }

        int numSelectTwo = 2;
        LotterySystem.lotteryDraw(list, numSelectTwo);

        LotterySystem.lotteryDraw(list, numSelect);

        int totalChanges = 0;
        for (Pair<User, WaitingListState> pair : list) {
            if (pair.second != WaitingListState.SELECTED && pair.second != WaitingListState.NOT_SELECTED) totalChanges++;
        }
        assertEquals(0, totalChanges);
    }

    @Test
    public void testNoEligibleCandidates() {
        list.clear();
        list.add(new Pair<>(user1, WaitingListState.ACCEPTED));
        list.add(new Pair<>(user2, WaitingListState.DECLINED));
        list.add(new Pair<>(user3, WaitingListState.CANCELED));

        try {
            LotterySystem.lotteryDraw(list, 2);
        } catch (Exception e) {
            fail("lotterySelect() should handle no eligible candidates safely, but threw: " + e.getMessage());
        }

        assertEquals(WaitingListState.ACCEPTED, list.get(0).second);
        assertEquals(WaitingListState.DECLINED, list.get(1).second);
        assertEquals(WaitingListState.CANCELED, list.get(2).second);
    }

    @Test
    public void testDrawWhenSlotsEqualCandidates() {
        list.clear();
        list.add(new Pair<>(user1, WaitingListState.ENTERED));
        list.add(new Pair<>(user2, WaitingListState.NOT_SELECTED));
        list.add(new Pair<>(user3, WaitingListState.ENTERED));

        LotterySystem.lotteryDraw(list, 3);

        assertEquals(WaitingListState.SELECTED, list.get(0).second);
        assertEquals(WaitingListState.SELECTED, list.get(1).second);
        assertEquals(WaitingListState.SELECTED, list.get(2).second);
    }
}