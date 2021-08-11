package ru.wb.perevozka.network.api.auth.response

import com.google.gson.annotations.SerializedName

data class CheckCouriersPhoneResponse (
    @SerializedName("remainingAttempts") var remainingAttempts: Int,
)