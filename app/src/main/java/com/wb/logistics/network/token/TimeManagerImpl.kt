package com.wb.logistics.network.token

import com.wb.logistics.app.AppPreffsKeys
import com.wb.logistics.utils.LogUtils
import com.wb.logistics.utils.prefs.SharedWorker
import com.wb.logistics.utils.time.TimeFormatter
import org.joda.time.DateTime

class TimeManagerImpl(private val worker: SharedWorker, private val timeFormatter: TimeFormatter) :
    TimeManager {

    override fun saveNetworkTime(time: String) {
        worker.save(AppPreffsKeys.TIME_NETWORK_KEY,
            timeFormatter.dateTimeWithTimezoneFromString(time).millis)
        LogUtils { logDebugApp("1. " + time) }
        LogUtils { logDebugApp("2. " + timeFormatter.dateTimeWithTimezoneFromString(time)) }
        worker.save(AppPreffsKeys.TIME_LOCAL_KEY, timeFormatter.currentDateTime().millis)
        LogUtils { logDebugApp("3. " + timeFormatter.currentDateTime()) }
    }

    override fun getOffsetLocalTime(): String {
        val networkTime = worker.load(AppPreffsKeys.TIME_NETWORK_KEY, 0L)
        val currentTime = timeFormatter.currentDateTime().millis
        val localTime = worker.load(AppPreffsKeys.TIME_LOCAL_KEY, 0L)
        val offsetTime = networkTime + (currentTime - localTime)
        val offsetDateTime = DateTime(offsetTime).toString()
        LogUtils { logDebugApp("4. " + offsetDateTime) }
        return offsetDateTime
    }

    override fun clear() {
        worker.delete(AppPreffsKeys.TIME_NETWORK_KEY)
        worker.delete(AppPreffsKeys.TIME_LOCAL_KEY)
    }

}