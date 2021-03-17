package com.wb.logistics.ui.auth

import android.content.Context
import com.wb.logistics.R
import java.util.*

class AuthResourceProvider(private val context: Context) {

    fun getTitleTemporaryPassword(phone: String) = context.getString(R.string.auth_temporary_password_title, phone)
    fun getTitleInputPassword(phone: String) = context.getString(R.string.auth_input_password_title, phone)
    fun getTitleInputTimerSpan() = context.getString(R.string.app_duration_time_span)
    fun getNumberAttempt(number: Int) = context.getString(
        R.string.auth_temporary_number_attempt,
        number
    )
    fun getSignUpTimeConfirmCode(min: Int, sec: Int) =
         context.getString(
            R.string.app_duration_time_value,
            String.format(Locale.getDefault(), "%02d", min),
            String.format(Locale.getDefault(), "%02d", sec)
        )

}