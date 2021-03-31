package com.wb.logistics.utils.time

import com.wb.logistics.app.AppConsts
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import java.text.DateFormatSymbols
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class TimeFormatterImpl : TimeFormatter {
    private val dateFormatSymbols: DateFormatSymbols = DateFormatSymbols(Locale(LOCALE_RU))
    override fun format(date: String, formatType: String): String {
        val calendar = calendarFromString(date)
        val dateTime = DateTime(calendar.timeInMillis)
        return format(dateTime, formatType)
    }

    override fun format(time: DateTime, formatType: String): String {
        return when (formatType) {
            TimeFormatType.ONLY_DATE -> formatDate(time, ONLY_DATE)
            TimeFormatType.ONLY_DATE_YMD -> formatDate(
                time,
                ONLY_DATE_YMD,
                DateTimeZone.forID(AppConsts.SERVER_TIMEZONE)
            )
            TimeFormatType.DAY_LETTER_MONTH_YEAR -> formatDate(
                time,
                DAY_LETTER_MONTH_YEAR
            )
            TimeFormatType.ONLY_TIME -> formatDate(time, ONLY_TIME)
            TimeFormatType.ONLY_MONTH -> formatDate(time, ONLY_MONTH)
            TimeFormatType.MIN_AND_SEC -> formatDate(time, MIN_AND_SEC)
            TimeFormatType.DAY_AND_LETTER_MONTH -> formatDayLetterMonth(time)
            TimeFormatType.HUMAN_DATE -> formatHumanDate(time)
            TimeFormatType.FULL_DATE_AND_TIME -> formatFullDateTime(time)
            TimeFormatType.DATE_AND_TIME -> formatDate(time, DATE_AND_TIME)
            else -> formatDate(time, DATE_AND_TIME)
        }
    }

    private fun formatDate(input: DateTime, timeFormat: String): String {
        val fmt = DateTimeFormat.forPattern(timeFormat)
        return fmt.print(input)
    }

    private fun formatDate(
        input: DateTime,
        timeFormat: String,
        timeZone: DateTimeZone,
    ): String {
        val fmt = DateTimeFormat.forPattern(timeFormat)
        return fmt.print(input.withZone(timeZone))
    }

    private fun formatDayLetterMonth(input: DateTime): String {
        val day = input.dayOfMonth
        val month = dateFormatSymbols.months[input.monthOfYear - 1]
        return String.format(Locale.getDefault(), "%d %s", day, month)
    }

    private fun formatHumanDate(time: DateTime): String {
        if (time.toLocalDate() == LocalDate()) {
            return TODAY
        } else if (time.toLocalDate() == LocalDate().minusDays(1)) {
            return YESTERDAY
        }
        return formatDate(time, DATE_AND_TIME)
    }

    private fun formatFullDateTime(time: DateTime): String {
        return String.format(
            Locale.getDefault(), FULL_DATE_TIME_FORMAT,
            formatDate(time, DAY_LETTER_MONTH_YEAR),
            formatDate(time, ONLY_TIME)
        )
    }

    override fun format(seconds: Long, @TimeFormatType formatType: String): String {
        return when (formatType) {
            TimeFormatType.MIN_AND_SEC -> formatInterval(
                seconds,
                MIN_AND_SEC
            )
            else -> formatInterval(seconds, MIN_AND_SEC)
        }
    }

    private fun formatInterval(seconds: Long, minAndSec: String): String {
        val dateTimeFormatter = DateTimeFormat.forPattern(minAndSec)
        return dateTimeFormatter.print(seconds * MILLIS_IN_SECOND)
    }

    override fun calendarFromString(date: String): Calendar {
        return getCalendar(date, PATTERN_CALENDAR_FORMAT)
    }

    override fun calendarFromStringSimple(date: String): Calendar {
        return getCalendar(date, PATTERN_SIMPLE_CALENDAR_FORMAT)
    }

    override fun calendarWithTimezoneFromString(date: String): Calendar {
        return getCalendar(date, PATTERN_SIMPLE_CALENDAR_WITH_TIMEZONE_FORMAT)
    }

    override fun calendarWithoutTimezoneFromString(date: String): Calendar {
        return getCalendar(date, PATTERN_CALENDAR_WITHOUT_TIMEZONE_FORMAT)
    }

    private fun getCalendar(date: String, format: String): Calendar {
        val calendar = Calendar.getInstance()
        val simpleDateFormat = SimpleDateFormat(format, Locale.getDefault())
        simpleDateFormat.timeZone = TimeZone.getTimeZone(TIME_ZONE_GMT)
        var parseDate: Date? = Date(System.currentTimeMillis())
        try {
            parseDate = simpleDateFormat.parse(date)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        parseDate?.apply {
            calendar.timeInMillis = parseDate.time
        }
        return calendar
    }

    override fun dateFromString(date: String): Date {
        return calendarFromString(date).time
    }

    override fun dateTimeFromString(date: String): DateTime {
        return DateTime(calendarFromString(date).timeInMillis)
    }

    override fun dateTimeFromStringSimple(date: String): DateTime {
        return DateTime(calendarFromStringSimple(date).timeInMillis)
    }

    override fun dateTimeWithTimezoneFromString(date: String): DateTime {
        return DateTime(calendarWithTimezoneFromString(date).timeInMillis)
    }

    override fun dateTimeWithoutTimezoneFromString(date: String): DateTime {
        return DateTime(calendarWithoutTimezoneFromString(date).timeInMillis)
    }

    companion object {
        private const val DATE_AND_TIME = "dd.MM.yyyy HH:mm"
        private const val DAY_LETTER_MONTH_YEAR = "d MMMM yyyy"
        private const val ONLY_DATE = "dd.MM.yyyy"
        private const val ONLY_DATE_YMD = "yyyy-MM-dd"
        private const val ONLY_TIME = "HH:mm"
        private const val ONLY_MONTH = "MMMM"
        private const val MIN_AND_SEC = "mm:ss"
        private const val TODAY = "Сегодня"
        private const val YESTERDAY = "Вчера"
        private const val LOCALE_RU = "RU"
        private const val FULL_DATE_TIME_FORMAT = "%s в %s"
        private const val MILLIS_IN_SECOND = 1000

        private const val TIME_ZONE_GMT = "GMT"

        private const val PATTERN_CALENDAR_FORMAT = "yyyy-MM-dd'T'HH:mm:ss"
        private const val PATTERN_SIMPLE_CALENDAR_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS"
        private const val PATTERN_CALENDAR_WITHOUT_TIMEZONE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss"
        private const val PATTERN_SIMPLE_CALENDAR_WITH_TIMEZONE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ"
    }

}