package ru.wb.perevozka.utils.time;

import androidx.annotation.NonNull;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class DateTimeFormatter {

    private final static String DATE_TIME_PATTERN = "d/MM/yyyy HH:mm:ss";
    private final static String DATE_PATTERN = "dd/MM/yyyy HH:mm:ss";

    public static DateTime parseDateTime(@NonNull String input) {
        return DateTime.parse(input, DateTimeFormat.forPattern(DATE_TIME_PATTERN));
    }

    public static DateTime parseDate(@NonNull String input) {
        return DateTime.parse(input, DateTimeFormat.forPattern(DATE_PATTERN));
    }

    public static DateTime currentDateTime() {
        return new DateTime();
    }

    public static Calendar getCalendarFromPeriod(int period) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(period));
        return calendar;
    }

}
