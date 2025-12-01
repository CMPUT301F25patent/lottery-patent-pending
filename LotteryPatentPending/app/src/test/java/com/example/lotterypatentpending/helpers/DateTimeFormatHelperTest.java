package com.example.lotterypatentpending.helpers;

import com.google.firebase.Timestamp;

import org.junit.Test;

import java.util.Date;
import java.util.Locale;

import static org.junit.Assert.*;

public class DateTimeFormatHelperTest {

    @Test
    public void testFormatTimestamp_Null_ReturnsNotSet() {
        assertEquals("Not set", DateTimeFormatHelper.formatTimestamp(null));
    }

    @Test
    public void testFormatTimestamp_FormatsCorrectly() {
        // 1 Jan 2024 10:30 UTC
        Date date = new Date(1704105000000L);
        Timestamp ts = new Timestamp(date);

        String formatted = DateTimeFormatHelper.formatTimestamp(ts);

        assertNotNull(formatted);
        assertTrue(formatted.contains("2024"));
        assertTrue(formatted.contains("1") || formatted.contains("1"));
    }

    @Test
    public void testParseTimestamp_Null_ReturnsNull() {
        assertNull(DateTimeFormatHelper.parseTimestamp(null));
        assertNull(DateTimeFormatHelper.parseTimestamp(""));
        assertNull(DateTimeFormatHelper.parseTimestamp("   "));
    }

    @Test
    public void testParseTimestamp_InvalidString_ReturnsNull() {
        assertNull(DateTimeFormatHelper.parseTimestamp("Not a real date"));
        assertNull(DateTimeFormatHelper.parseTimestamp("32/50/9999 99:99 AM"));
    }

    @Test
    public void testParseTimestamp_ParsesCorrectly() {
        String input = "05/02/2024 03:15 PM";

        Timestamp ts = DateTimeFormatHelper.parseTimestamp(input);

        assertNotNull(ts);
        assertNotEquals(0, ts.getSeconds());
    }

    @Test
    public void testRoundTrip_FormatParse() {
        Date now = new Date();
        Timestamp ts = new Timestamp(now);

        String formatted = DateTimeFormatHelper.formatTimestamp(ts);
        Timestamp parsed = DateTimeFormatHelper.parseTimestamp(formatted);

        assertNotNull(parsed);

        long diff = Math.abs(parsed.toDate().getTime() - ts.toDate().getTime());

        assertTrue("Round-trip difference too large: " + diff, diff < 1000);
    }

    @Test
    public void testFormattingRespectsDisplayPattern() {
        Date date = new Date(1704105000000L);
        Timestamp ts = new Timestamp(date);

        String out = DateTimeFormatHelper.formatTimestamp(ts);

        assertTrue(out.matches("\\d{2}/\\d{2}/\\d{4} \\d{2}:\\d{2} (AM|PM)"));
    }
}
