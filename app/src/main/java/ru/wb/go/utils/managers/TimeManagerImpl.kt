package ru.wb.go.utils.managers

import android.annotation.SuppressLint
import org.joda.time.DateTime
import ru.wb.go.app.AppPreffsKeys
import ru.wb.go.utils.prefs.SharedWorker
import ru.wb.go.utils.time.TimeFormatType.DATE_AND_TIME
import ru.wb.go.utils.time.TimeFormatter
import java.text.SimpleDateFormat
import java.util.*


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
        return timeFormatter.dateTimeWithoutTimezoneOffsetFromString(
            dateTime,
            worker.load(AppPreffsKeys.OFFSET_LOCAL_DATE_TIME_KEY, 0L)
        ).toString()
    }

    override fun clear() {
        worker.delete(AppPreffsKeys.SERVER_DATE_TIME_KEY)
        worker.delete(AppPreffsKeys.LOCAL_DATE_TIME_KEY)
//        worker.delete(AppPreffsKeys.STARTED_TASK_TIME_KEY)
    }

    @SuppressLint("SimpleDateFormat")
    override fun getLocalTime(): String {
        return SimpleDateFormat("yyyy-MM-dd'T'H:mm:ssZZZZZ").format(Date())
//        return timeFormatter.currentDateTime().toString()
    }

    override fun getLocalDateAndTime(): String {
        return timeFormatter.currentDateTimeFormat(DATE_AND_TIME)
    }

    override fun getPassedTime(startTime: String): Long {
        val start =
            timeFormatter.dateTimeWithoutTimezoneFromString(startTime).millis

        return (timeFormatter.currentDateTime().millis - start) / 1000
    }

//    override fun saveStartedTaskTime(dateTime: String) {
//        worker.save(AppPreffsKeys.STARTED_TASK_TIME_KEY, dateTime)
//    }
//
//    override fun getStartedTaskTime(): String {
//        return worker.load(AppPreffsKeys.STARTED_TASK_TIME_KEY, "")
//    }

}