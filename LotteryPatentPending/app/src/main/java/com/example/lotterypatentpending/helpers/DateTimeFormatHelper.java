package com.example.lotterypatentpending.helpers;

import com.google.firebase.Timestamp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * returns a formatted time from (UTC) to local time.
 */
public class DateTimeFormatHelper {

    public static final String DISPLAY_PATTERN = "dd/MM/yyyy hh:mm a";

    private DateTimeFormatHelper() {}

    /**
     *
     * @param ts Timestamp(UTC)
     * @return Local time in string
     */
    public static String formatTimestamp(Timestamp ts) {
        if (ts == null) {
            return "Not set";
        }
        Date date = ts.toDate();
        SimpleDateFormat sdf =
                new SimpleDateFormat(DISPLAY_PATTERN, Locale.getDefault());
        return sdf.format(date); // local time
    }


    /**
     *
     * @param input String of local time
     * @return Timestamp of UTC converted from local time
     */
    public static Timestamp parseTimestamp(String input) {
        if (input == null || input.trim().isEmpty()) return null;

        try {
            SimpleDateFormat sdf =
                    new SimpleDateFormat(DISPLAY_PATTERN, Locale.getDefault());
            Date parsed = sdf.parse(input.trim());

            if (parsed == null) return null;
            return new Timestamp(parsed);
        } catch (ParseException e) {
            return null;
        }
    }

}
