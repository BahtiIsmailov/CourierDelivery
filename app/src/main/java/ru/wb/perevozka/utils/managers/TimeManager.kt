package ru.wb.perevozka.utils.managers

interface TimeManager {
    fun saveNetworkTime(dateTime: String)
    fun getOffsetLocalTime(): String
    fun getOffsetTimeZone(dateTime: String): String
    fun clear()
    fun getLocalTime(): String
}