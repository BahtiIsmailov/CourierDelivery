package ru.wb.go.ui

import androidx.lifecycle.ViewModel
import ru.wb.go.utils.analytics.YandexMetricManager

abstract class NetworkViewModel(
    private val metric: YandexMetricManager,
) :
    ViewModel() {

    abstract fun getScreenTag(): String


    fun onTechEventLog(method: String, message: String = EMPTY_MESSAGE) {
        metric.onTechEventLog(getScreenTag(), method, message)
    }

    fun onTechErrorLog(method: String, error: Throwable) {
        metric.onTechErrorLog(getScreenTag(), method, error.toString())
    }

    companion object {
        const val EMPTY_MESSAGE = ""
    }

}

