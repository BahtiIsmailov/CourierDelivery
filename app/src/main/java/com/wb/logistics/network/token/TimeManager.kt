package com.wb.logistics.network.token

interface TimeManager {
    fun saveNetworkTime(time: String)
    fun getOffsetLocalTime(): String
    fun clear()
}