package com.wb.logistics.utils.time

import org.joda.time.DateTime
import java.util.*

interface TimeFormatter {
    fun format(date: String, formatType: String): String
    fun format(time: DateTime, @TimeFormatType formatType: String): String
    fun format(seconds: Long, @TimeFormatType formatType: String): String
    fun calendarFromString(date: String): Calendar
    fun calendarFromStringSimple(date: String): Calendar
    fun calendarWithoutTimezoneFromString(date: String): Calendar
    fun dateFromString(date: String): Date
    fun dateTimeFromString(date: String): DateTime
    fun dateTimeFromStringSimple(date: String): DateTime
    fun dateTimeWithoutTimezoneFromString(date: String): DateTime
}