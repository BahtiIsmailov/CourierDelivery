package ru.wb.perevozka.utils.time

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import ru.wb.perevozka.app.ARRIVAL_TIME_COURIER_SEC
import ru.wb.perevozka.ui.auth.CheckSmsViewModel
import ru.wb.perevozka.ui.courierloading.CourierLoadingScanTimerState
import java.util.*
import java.util.concurrent.TimeUnit

object DateTimeFormatter {

    private const val DATE_TIME_PATTERN = "d/MM/yyyy HH:mm:ss"
    private const val DATE_PATTERN = "dd/MM/yyyy HH:mm:ss"


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

    private fun formatLeadingZero(duration: Int) =
        String.format(Locale.getDefault(), "%02d", duration)

    fun getAnalogTime(duration: Int) = (100.0F / ARRIVAL_TIME_COURIER_SEC) * duration

    private fun getMin(duration: Int): Int {
        return duration / CheckSmsViewModel.TIME_DIVIDER
    }

    private fun getSec(duration: Int): Int {
        return duration % CheckSmsViewModel.TIME_DIVIDER
    }

}