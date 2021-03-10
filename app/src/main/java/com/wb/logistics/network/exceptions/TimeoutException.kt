package com.wb.logistics.network.exceptions

class TimeoutException(override val message: String) : Exception(TAG) {

    companion object {
        const val TAG = "TimeoutException"
    }

}