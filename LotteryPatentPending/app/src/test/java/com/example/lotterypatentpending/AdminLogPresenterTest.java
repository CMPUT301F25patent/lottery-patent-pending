package com.example.lotterypatentpending;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.lotterypatentpending.models.AdminLogPresenter;
import com.example.lotterypatentpending.models.NotificationLog;
import com.example.lotterypatentpending.models.Notification.Category;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

public class AdminLogPresenterTest {

    private static NotificationLog mk(Category cat, Date when) {
        NotificationLog n = new NotificationLog();
        n.setOrganizerId("org1");
        n.setEventId("evt1");
        n.setCategory(cat);
        n.setRecipientIds(Arrays.asList("u1","u2"));
        n.setPayloadPreview("Body preview");
        n.setCreatedAt(when);
        return n;
    }

    private static String repeat(char ch, int n) {
        char[] arr = new char[n];
        java.util.Arrays.fill(arr, ch);
        return new String(arr);
    }

    @Test
    public void title_waitlist() {
        NotificationLog n = mk(Category.WAITLIST, null);
        assertEquals("WAITLIST • evt1", AdminLogPresenter.formatTitle(n));
    }

    @Test
    public void body_previewTruncation() {
        NotificationLog n = mk(Category.WAITLIST, null);
        n.setPayloadPreview(repeat('x', 120));
        String body = AdminLogPresenter.formatBody(n);
        assertTrue(body.endsWith("…"));
        assertTrue(body.length() <= 100);
    }

    @Test
    public void meta_containsTime() {
        Calendar c = Calendar.getInstance();
        c.set(2025, Calendar.JANUARY, 2, 3, 4, 0);
        NotificationLog n = mk(Category.SELECTED, c.getTime());
        String meta = AdminLogPresenter.formatMeta(n);
        assertTrue(meta.matches(".*\\b\\d{2}:\\d{2}\\b.*"));
        assertTrue(meta.contains("org=org1"));
    }
}
