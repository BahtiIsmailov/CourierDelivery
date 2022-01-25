package ru.wb.go.utils.managers

import android.content.Context
import ru.wb.go.R
import ru.wb.go.network.exceptions.CustomException
import ru.wb.go.network.exceptions.HttpPageNotFound
import ru.wb.go.network.exceptions.NoInternetException
import ru.wb.go.network.exceptions.TimeoutException
import ru.wb.go.ui.SingleLiveEvent
import ru.wb.go.ui.dialogs.DialogInfoFragment
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
            else -> {
                assert(throwable.message != "")
                ErrorDialogData(
                    dlgTag = dlgTag,
                    type = DialogInfoStyle.ERROR.ordinal,
                    title = context.getString(R.string.error_title),
                    message = throwable.message ?: throwable.toString()
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