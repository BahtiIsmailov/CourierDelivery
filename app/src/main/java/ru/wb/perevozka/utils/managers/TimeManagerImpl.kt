package ru.wb.perevozka.utils.managers

import ru.wb.perevozka.app.AppPreffsKeys
import ru.wb.perevozka.utils.prefs.SharedWorker
import ru.wb.perevozka.utils.time.TimeFormatter
import org.joda.time.DateTime


class TimeManagerImpl(private val worker: SharedWorker, private val timeFormatter: TimeFormatter) :
    TimeManager {

    override fun saveNetworkTime(dateTime: String) {
        val serverDataTime = timeFormatter.dateTimeWithoutTimezoneFromString(dateTime).millis
        worker.save(AppPreffsKeys.SERVER_DATE_TIME_KEY, serverDataTime)
        val currentDateTime = timeFormatter.currentDateTime().millis
        worker.save(AppPreffsKeys.LOCAL_DATE_TIME_KEY, currentDateTime)
        val offsetDateTime = currentDateTime - serverDataTime
        worker.save(AppPreffsKeys.OFFSET_LOCAL_DATE_TIME_KEY, offsetDateTime)
    }

    override fun getOffsetLocalTime(): String {
        val serverTime = worker.load(AppPreffsKeys.SERVER_DATE_TIME_KEY, 0L)
        val localTime = worker.load(AppPreffsKeys.LOCAL_DATE_TIME_KEY, 0L)
        val currentTime = timeFormatter.currentDateTime().millis
        val offsetTimeZone = worker.load(AppPreffsKeys.OFFSET_LOCAL_DATE_TIME_KEY, 0L)
        val offsetDateTime = serverTime + (currentTime - localTime) + offsetTimeZone
        return DateTime(offsetDateTime).toString()
    }

    override fun getOffsetTimeZone(dateTime: String): String {
        return timeFormatter.dateTimeWithoutTimezoneOffsetFromString(dateTime,
            worker.load(AppPreffsKeys.OFFSET_LOCAL_DATE_TIME_KEY, 0L)).toString()
    }

    override fun clear() {
        worker.delete(AppPreffsKeys.SERVER_DATE_TIME_KEY)
        worker.delete(AppPreffsKeys.LOCAL_DATE_TIME_KEY)
    }

    override fun getLocalTime(): String {
        return timeFormatter.currentDateTime().toString()
    }

}