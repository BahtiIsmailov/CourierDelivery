package ru.wb.go.utils.managers

import android.content.Context
import retrofit2.HttpException
import ru.wb.go.R
import ru.wb.go.network.exceptions.*
import ru.wb.go.ui.SingleLiveEvent
import ru.wb.go.ui.dialogs.DialogInfoStyle
import java.net.UnknownHostException


class ErrorDialogManagerImpl(val context: Context) : ErrorDialogManager {

    override fun showErrorDialog(
        error: Throwable,
        errorData: SingleLiveEvent<ErrorDialogData>,
        dlgTag: String
    ) {
        val data = when (error) {
            is TimeoutException -> {
                ErrorDialogData(
                    dlgTag = dlgTag,
                    type = DialogInfoStyle.WARNING.ordinal,
                    title = context.getString(R.string.error_title),
                    message = context.getString(R.string.http_timeout_error)
                )
            }
            is NoInternetException, is UnknownHostException -> {
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
                    message = error.message!!
                )
            }
            is BadRequestException -> {
                val msg = error.error.toString()
                ErrorDialogData(
                    dlgTag = dlgTag,
                    type = DialogInfoStyle.ERROR.ordinal,
                    title = context.getString(R.string.error_title),
                    message = (error.message ?: error.toString()) + msg
                )
            }
            is HttpException -> {
                if (error.code() >= 500) {
                    ErrorDialogData(
                        dlgTag = dlgTag,
                        type = DialogInfoStyle.ERROR.ordinal,
                        title = context.getString(R.string.error_service, error.code()),
                        message = error.message ?: error.toString()
                    )
                } else {
                    ErrorDialogData(
                        dlgTag = dlgTag,
                        type = DialogInfoStyle.ERROR.ordinal,
                        title = context.getString(R.string.error_title),
                        message = (error.message ?: error.toString())
                    )
                }
            }
            else -> {
                ErrorDialogData(
                    dlgTag = dlgTag,
                    type = DialogInfoStyle.ERROR.ordinal,
                    title = context.getString(R.string.error_title),
                    message = (error.message ?: error.toString())
                )

            }
        }
        errorData.postValue(data)
    }
}


data class ErrorDialogData(
    val dlgTag: String,
    val type: Int,
    val title: String,
    val message: String
)