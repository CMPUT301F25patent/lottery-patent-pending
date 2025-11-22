
package com.example.lotterypatentpending.helpers;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.widget.TextView;

import com.google.firebase.Timestamp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * author Erik
 * contributor Erik
 */
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
     */

    public static void attachDateTimePicker(TextView target, Context context) {
        target.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();

            // 1) Show DATE picker
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    context,
                    (view, year, month, dayOfMonth) -> {
                        // 2) User has PICKED a DATE here
                        cal.set(Calendar.YEAR, year);
                        cal.set(Calendar.MONTH, month);
                        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        // 3) Now show TIME picker
                        TimePickerDialog timeDialog = new TimePickerDialog(
                                context,
                                (timeView, hourOfDay, minute) -> {
                                    // 4) User has PICKED a TIME here
                                    cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                    cal.set(Calendar.MINUTE, minute);
                                    cal.set(Calendar.SECOND, 0);

                                    // 5) NOW we format and set the TextView
                                    SimpleDateFormat sdf =
                                            new SimpleDateFormat(DISPLAY_PATTERN, Locale.getDefault());
                                    String formatted = sdf.format(cal.getTime());
                                    target.setText(formatted);  // set the textViews to the formatted values
                                },
                                // set initial/default values for time
                                cal.get(Calendar.HOUR_OF_DAY),
                                cal.get(Calendar.MINUTE),
                                false
                        );

                        timeDialog.show();
                    },
                    // set initial/default values for date
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
            );

            //Set min date
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000L);
            datePickerDialog.show();
        });

    }


}
