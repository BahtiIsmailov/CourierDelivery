package ru.wb.go.utils.managers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import ru.wb.go.R
import ru.wb.go.network.exceptions.*
import ru.wb.go.ui.SingleLiveEvent
import ru.wb.go.ui.app.AppActivity
import ru.wb.go.ui.dialogs.DialogInfoStyle
import kotlin.system.exitProcess


class ErrorDialogManagerImpl(val context: Context) : ErrorDialogManager {

    override fun showErrorDialog(
        throwable: Throwable,
        errorData: SingleLiveEvent<ErrorDialogData>,
        dlgTag: String
    ) {
        val data = when (throwable) {
            is TimeoutException -> {
                ErrorDialogData(
                    dlgTag = dlgTag,
                    type = DialogInfoStyle.WARNING.ordinal,
                    title = context.getString(R.string.error_title),
                    message = context.getString(R.string.http_timeout_error)
                )
            }
            is NoInternetException -> {
                ErrorDialogData(
                    dlgTag = dlgTag,
                    type = DialogInfoStyle.WARNING.ordinal,
                    title = context.getString(R.string.error_title),
                    message = context.getString(R.string.unknown_internet_title_error)
                )
            }
            is CustomException, is HttpPageNotFoundException -> {
                ErrorDialogData(
                    dlgTag = dlgTag,
                    type = DialogInfoStyle.WARNING.ordinal,
                    title = context.getString(R.string.attention_title),
                    message = throwable.message!!
                )
            }
            is BadRequestException->{
                val msg = throwable.error.toString()
                ErrorDialogData(
                    dlgTag = dlgTag,
                    type = DialogInfoStyle.ERROR.ordinal,
                    title = context.getString(R.string.error_title),
                    message = (throwable.message ?: throwable.toString()) + msg
                )
            }
            else -> {
                ErrorDialogData(
                    dlgTag = dlgTag,
                    type = DialogInfoStyle.ERROR.ordinal,
                    title = context.getString(R.string.error_title),
                    message = context.getString(R.string.http_400_exception)
                )
            }
        }

        errorData.postValue(data)
    }
}


data class ErrorDialogData(
    val dlgTag: String,
    val type: Int, val title: String, val message: String
)