package com.wb.logistics.network.exceptions

import com.google.gson.annotations.SerializedName

class ApiGeneralErrorResponse {
    @SerializedName("message")
    var apiMessage: String? = null
}