package ru.wb.perevozka.ui.auth.courierdata

import android.content.Context
import ru.wb.perevozka.R
import ru.wb.perevozka.mvvm.BaseMessageResourceProvider
import java.util.*

class CourierDataResourceProvider(private val context: Context): BaseMessageResourceProvider(context) {

    fun getTitleTemporaryPassword(phone: String) = context.getString(R.string.auth_temporary_password_title, phone)
    fun getTitleInputTimerSpan() = context.getString(R.string.app_duration_time_span)

    fun getSignUpTimeConfirmCode(min: Int, sec: Int) =
         context.getString(
            R.string.app_duration_time_value,
            String.format(Locale.getDefault(), "%02d", min),
            String.format(Locale.getDefault(), "%02d", sec)
        )

    fun getNumberNotFound() = context.getString(R.string.auth_number_phone_phone_not_found)
    fun getPasswordNotFound() = context.getString(R.string.auth_input_password_not_found)
    fun getTemporaryPasswordNotFound() = context.getString(R.string.auth_temporary_password_incorrect)

}