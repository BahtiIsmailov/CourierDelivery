package ru.wb.perevozka.ui.userdata

import android.content.Context
import ru.wb.perevozka.R
import java.util.*

class UserDataResourceProvider(private val context: Context) {

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

    fun getGenericError() = context.getString(R.string.unknown_generic_error)

}