package com.wb.logistics.ui.reception

sealed class ReceptionHandleUIState<out R> {
    data class BoxFormatted(val number: String) : ReceptionHandleUIState<Nothing>()
    data class BoxAcceptDisabled(val number: String) : ReceptionHandleUIState<Nothing>()
}