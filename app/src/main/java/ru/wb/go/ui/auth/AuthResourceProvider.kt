package ru.wb.go.ui.auth

import android.content.Context
import ru.wb.go.R
import ru.wb.go.mvvm.BaseMessageResourceProvider
import java.util.*

class AuthResourceProvider(private val context: Context) :
    BaseMessageResourceProvider(context) {

    fun getTitleCheckSms(phone: String) = context.getString(R.string.auth_check_sms_title, phone)
    fun getTitleInputTimerSpan() = context.getString(R.string.app_duration_time_span)

    fun getSignUpTimeConfirmCode(min: Int, sec: Int) =
        context.getString(
            R.string.app_duration_time_value,
            String.format(Locale.getDefault(), "%02d", min),
            String.format(Locale.getDefault(), "%02d", sec)
        )

}