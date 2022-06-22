package ru.wb.go.utils.managers

import ru.wb.go.ui.SingleLiveEvent
import ru.wb.go.ui.dialogs.DialogInfoFragment

interface ErrorDialogManager {
    fun showErrorDialog(
        error: Throwable,
        errorData: SingleLiveEvent<ErrorDialogData>,
        dlgTag: String = DialogInfoFragment.DIALOG_INFO_TAG
    )

}