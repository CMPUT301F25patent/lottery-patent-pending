
package com.example.lotterypatentpending.helpers;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.Timestamp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateTimePickerHelper {

    private static final String DISPLAY_PATTERN = "dd/MM/yyyy hh:mm a";

    private DateTimePickerHelper() {
        // utility class, no instances
    }

    /**
     * Attach a date+time picker to an TextView.
     *
     * @param target     the TextView to fill
     * @param context    context (e.g. requireContext())
     * @param futureOnly if true, disallow past date/time
     * @param minHour    nullable; e.g. 8 for 8:00. Use null for no lower bound
     * @param maxHour    nullable; e.g. 22 for 22:00. Use null for no upper bound
     */

    public static void attachDateTimePicker(TextView target, Context context,
                                            boolean futureOnly,
                                            Integer minHour,
                                            Integer maxHour) {
        target.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    context,
                    (view, year, month, dayOfMonth) -> {
                        cal.set(Calendar.YEAR, year);
                        cal.set(Calendar.MONTH, month);
                        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        TimePickerDialog timeDialog = new TimePickerDialog(context,
                                (timeView, hourOfDay, minute) -> {
                                    if (minHour != null && hourOfDay < minHour) {
                                        hourOfDay = minHour;
                                    }

                                    if (maxHour != null && hourOfDay > maxHour) {
                                        hourOfDay = maxHour;
                                    }
                                    cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                    cal.set(Calendar.MINUTE, minute);
                                    cal.set(Calendar.SECOND, 0);

                                    long chosenSeconds = cal.getTimeInMillis() / 1000L;
                                    long nowSeconds = System.currentTimeMillis() / 1000L;

                                    if (futureOnly && chosenSeconds < nowSeconds) {
                                        Toast.makeText(context,
                                                "Please choose a future time",
                                                Toast.LENGTH_SHORT).show();
                                        target.setText("");
                                        return;
                                    }
                                    SimpleDateFormat sdf =
                                            new SimpleDateFormat(DISPLAY_PATTERN, Locale.getDefault());
                                    String formatted = sdf.format(cal.getTime());
                                    target.setText(formatted);
                                },
                                cal.get(Calendar.HOUR_OF_DAY),
                                cal.get(Calendar.MINUTE),
                                false
                        );

                        timeDialog.show();
                    },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
            );

            if (futureOnly) {
                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
            }
            datePickerDialog.show();
        });
    }

    public static void attachDateTimePicker(TextView target, Context context) {
        attachDateTimePicker(target, context, false, null, null);
    }

    public static Timestamp parseToTimestamp(String input) {
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
