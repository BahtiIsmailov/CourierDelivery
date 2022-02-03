package ru.wb.go.utils.managers

import android.content.Context
import ru.wb.go.R
import ru.wb.go.network.exceptions.*
import ru.wb.go.ui.SingleLiveEvent
import ru.wb.go.ui.dialogs.DialogInfoStyle

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
            is CustomException, is HttpPageNotFound -> {
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
                    message = (throwable.message ?: throwable.toString())
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