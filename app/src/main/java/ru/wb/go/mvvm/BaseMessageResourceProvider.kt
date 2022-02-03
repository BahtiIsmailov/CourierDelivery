package ru.wb.go.mvvm

import android.content.Context
import ru.wb.go.R

abstract class BaseMessageResourceProvider(private val context: Context) {

    fun getVersionApp(version: String) = context.getString(R.string.app_version, version)

    fun getGenericInternetTitleError() = context.getString(R.string.unknown_internet_title_error)
    fun getGenericInternetMessageError() = context.getString(R.string.unknown_internet_message_error)
    fun getGenericInternetButtonError() = context.getString(R.string.ok_button_title)

    fun getGenericServiceTitleError() = context.getString(R.string.error_title)
    fun getGenericServiceButtonError() = context.getString(R.string.ok_button_title)

}