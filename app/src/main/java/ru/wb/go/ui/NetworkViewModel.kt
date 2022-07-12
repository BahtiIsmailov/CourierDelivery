package ru.wb.go.ui

import androidx.lifecycle.ViewModel
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase

abstract class NetworkViewModel() : ViewModel() {

    abstract fun getScreenTag(): String


    fun onTechEventLog(method: String, message: String = EMPTY_MESSAGE) {
        //metric.onTechEventLog(getScreenTag(), method, message)
    }

    fun onTechErrorLog(method: String, error: Throwable) {
        //metric.onTechErrorLog(getScreenTag(), method, error.toString())
    }

    fun logException(throwable: Throwable,message: String) {
        Firebase.crashlytics.log(message)
        Firebase.crashlytics.recordException(throwable)
    }

    companion object {
        const val EMPTY_MESSAGE = ""
    }

}

