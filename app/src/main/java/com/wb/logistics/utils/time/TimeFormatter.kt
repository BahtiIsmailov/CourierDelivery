package com.wb.logistics.utils.time

import org.joda.time.DateTime
import java.util.*

interface TimeFormatter {

    fun currentDateTime(): DateTime
    fun currentDateTimeFormat(@TimeFormatType formatType: String): String

    fun format(date: String, @TimeFormatType formatType: String): String
    fun format(dateTime: DateTime, @TimeFormatType formatType: String): String
    fun format(seconds: Long, @TimeFormatType formatType: String): String
    fun dateFromString(date: String): Date
    fun dateTimeFromStringSimple(date: String): DateTime
    fun dateTimeWithTimezoneFromString(date: String): DateTime
    fun dateTimeWithoutTimezoneFromString(date: String): DateTime
    fun dateTimeWithoutTimezoneOffsetFromString(date: String, offset: Long): DateTime
}