package com.example.lotterypatentpending.helpers;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * returns a formatted time from (UTC) to local time.
 */
public class DateTimeFormatHelper {

    public static final String DISPLAY_PATTERN = "dd/MM/yyyy hh:mm a";

    private DateTimeFormatHelper() {}

    public static String formatTimestamp(Timestamp ts) {
        if (ts == null) {
            return "Not set";
        }
        Date date = ts.toDate();
        SimpleDateFormat sdf =
                new SimpleDateFormat(DISPLAY_PATTERN, Locale.getDefault());
        return sdf.format(date); // local time
    }

}
