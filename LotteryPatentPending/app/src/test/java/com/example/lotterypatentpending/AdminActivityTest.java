package com.example.lotterypatentpending;

import static org.junit.Assert.*;


import com.example.lotterypatentpending.models.User;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for AdminActivity logic-related behavior.
 * These tests DO NOT instantiate the actual Android Activity.
 * They only verify admin access rules and intent structure.
 */
public class AdminActivityTest {

    private User adminUser;
    private User normalUser;

    @Before
    public void setUp() {
        adminUser = new User("admin001", "System Admin", "admin@email.com", "N/A", true);
        normalUser = new User("user001", "Regular User", "user@email.com", "111-222-3333", false);
    }

    @Test
    public void testAdminUserHasPrivileges() {
        assertTrue(adminUser.isAdmin());
        assertEquals("System Admin", adminUser.getName());
        assertEquals("admin@email.com", adminUser.getEmail());
    }

    @Test
    public void testNormalUserHasNoPrivileges() {
        assertFalse(normalUser.isAdmin());
    }


}