package ru.wb.go.mvvm

import android.content.Context
import ru.wb.go.R

abstract class BaseMessageResourceProvider(private val context: Context) {

    fun getGenericInternetTitleError() = context.getString(R.string.unknown_internet_title_error)
    fun getGenericInternetMessageError() = context.getString(R.string.unknown_internet_message_error)
    fun getGenericInternetButtonError() = context.getString(R.string.unknown_internet_button_error)

    fun getGenericServiceTitleError() = context.getString(R.string.unknown_service_title_error)
    fun getGenericServiceButtonError() = context.getString(R.string.unknown_service_button_error)

}