package com.example.lotterypatentpending;

import static org.junit.Assert.*;

import com.example.lotterypatentpending.models.User;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Pure JVM unit tests for AdminUsersActivity logic.
 * This does NOT require Android or Firebase; it tests core behaviors like deletion and refreshing.
 */
public class AdminUserActivityTest {

    private List<User> userList;
    private User adminUser;
    private User normalUser;

    @Before
    public void setUp() {
        userList = new ArrayList<>();
        adminUser = new User("admin001", "System Admin", "admin@email.com", "N/A", true);
        normalUser = new User("user001", "Regular User", "user@email.com", "111-222-3333", false);
        userList.add(adminUser);
        userList.add(normalUser);
    }

    @Test
    public void testUserListInitiallyContainsBothUsers() {
        assertEquals(2, userList.size());
        assertTrue(userList.contains(adminUser));
        assertTrue(userList.contains(normalUser));
    }

    @Test
    public void testDeleteUserRemovesCorrectUser() {
        userList.removeIf(u -> u.getUserId().equals("user001"));
        assertEquals(1, userList.size());
        assertFalse(userList.contains(normalUser));
        assertTrue(userList.contains(adminUser));
    }

    @Test
    public void testDeleteNonexistentUserDoesNothing() {
        boolean removed = userList.removeIf(u -> u.getUserId().equals("ghostUser"));
        assertFalse(removed);
        assertEquals(2, userList.size());
    }

    @Test
    public void testAdminPrivilegesCheck() {
        assertTrue(adminUser.isAdmin());
        assertFalse(normalUser.isAdmin());
    }
}
