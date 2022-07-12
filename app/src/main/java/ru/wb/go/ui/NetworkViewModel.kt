package ru.wb.go.ui

import androidx.lifecycle.ViewModel

abstract class NetworkViewModel() : ViewModel() {

    abstract fun getScreenTag(): String


    fun onTechEventLog(method: String, message: String = EMPTY_MESSAGE) {
        //metric.onTechEventLog(getScreenTag(), method, message)
    }

    fun onTechErrorLog(method: String, error: Throwable) {
        //metric.onTechErrorLog(getScreenTag(), method, error.toString())
    }

    companion object {
        const val EMPTY_MESSAGE = ""
    }

}

