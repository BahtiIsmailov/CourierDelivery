package com.wb.logistics.network.exceptions

class NoInternetException(override val message: String) : Exception(TAG) {

    companion object {
        const val TAG = "NoInternetException"
    }
}