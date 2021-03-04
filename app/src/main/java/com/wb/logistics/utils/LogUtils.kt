package com.wb.logistics.utils

import android.util.Log
import com.wb.logistics.BuildConfig

class LogUtils(block: LogUtils.() -> Unit) {

    fun logDebug(owner: Any, message: String) {
        if (DEBUG_MODE) {
            Log.d(owner.javaClass.simpleName, message)
        }
    }

    fun logDebug(owner: String, message: String) {
        if (DEBUG_MODE) {
            Log.d(owner, message)
        }

    }

    fun logDebugApp(message: String) {
        if (DEBUG_MODE) {
            Log.d(Companion.DEBUG_APP_TAG, message)
        }
    }

    fun logError(owner: Any, message: String) {
        if (DEBUG_MODE) {
            Log.e(owner.javaClass.simpleName, message)
        }
    }

    fun logError(owner: Any, message: String, exception: Exception) {
        if (DEBUG_MODE) {
            Log.e(
                owner.javaClass.simpleName, """
     $message
     ${exception.message}
     """.trimIndent()
            )
        }
    }

    fun logInfo(message: String) {
        if (DEBUG_MODE) {
            Log.i(LogUtils::class.java.simpleName, message)
        }
    }

    fun logVerbose(owner: Any, message: String) {
        if (DEBUG_MODE) {
            Log.v(owner.javaClass.simpleName, message)
        }
    }

    companion object {
        private val DEBUG_MODE = BuildConfig.DEBUG
        private const val DEBUG_APP_TAG = "LOGISTIC/DEBUG"
    }

}