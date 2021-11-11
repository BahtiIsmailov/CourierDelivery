package ru.wb.go.utils.time

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.util.*
import java.util.concurrent.TimeUnit

object DateTimeFormatter {

    private const val DATE_TIME_PATTERN = "d/MM/yyyy HH:mm:ss"
    private const val DATE_PATTERN = "dd/MM/yyyy HH:mm:ss"
    private const val HOUR_DIVIDER = 60 * 60
    private const val TIME_DIVIDER = 60


    fun parseDateTime(input: String): DateTime {
        return DateTime.parse(input, DateTimeFormat.forPattern(DATE_TIME_PATTERN))
    }

    fun parseDate(input: String): DateTime {
        return DateTime.parse(input, DateTimeFormat.forPattern(DATE_PATTERN))
    }

    fun currentDateTime(): DateTime {
        return DateTime()
    }

    fun getCalendarFromPeriod(period: Int): Calendar {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis =
            System.currentTimeMillis() + TimeUnit.DAYS.toMillis(period.toLong())
        return calendar
    }

    fun getDigitTime(duration: Int): String {
        val min = formatLeadingZero(getMin(duration))
        val sec = formatLeadingZero(getSec(duration))
        return "$min:$sec"
    }

    fun getDigitFullTime(duration: Int): String {
        val hour = getHour(duration)
        val min = formatLeadingZero(getMin(duration))
        val sec = formatLeadingZero(getSec(duration))
        return "$hour:$min:$sec"
    }

    private fun formatLeadingZero(duration: Int) =
        String.format(Locale.getDefault(), "%02d", duration)

    fun getAnalogTime(arrival: Int, duration: Int) = (100.0F / arrival) * duration

    private fun getHour(duration: Int): String {
        val hourDivider = duration / HOUR_DIVIDER
        return if (hourDivider >= 24) "" + hourDivider / 24 + " д. " + formatLeadingZero(hourDivider % 24) else formatLeadingZero(hourDivider)
    }

    private fun getMin(duration: Int): Int {
        val minDivider = duration / TIME_DIVIDER
        return if (minDivider > TIME_DIVIDER) minDivider % TIME_DIVIDER else duration / TIME_DIVIDER
    }

    private fun getSec(duration: Int): Int {
        return duration % TIME_DIVIDER
    }

}