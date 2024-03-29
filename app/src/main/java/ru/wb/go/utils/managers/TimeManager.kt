package ru.wb.go.utils.managers

interface TimeManager {
    fun saveNetworkTime(dateTime: String)
    fun getOffsetLocalTime(): String
    fun getOffsetTimeZone(dateTime: String): String
    fun clear()
    fun getLocalTime(): String
    fun getLocalMetricTime(): String
    fun getLocalDateAndTime(): String
    fun getPassedTime(startTime:String):Long
}