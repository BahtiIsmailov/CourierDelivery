package com.wb.logistics.ui.dcunloading

sealed class DcUnloadingHandleUIState<out R> {
    data class BoxFormatted(val number: String) : DcUnloadingHandleUIState<Nothing>()
    data class BoxAcceptDisabled(val number: String) : DcUnloadingHandleUIState<Nothing>()
    object BoxesEmpty : DcUnloadingHandleUIState<Nothing>()
    data class BoxesComplete(val boxes: List<String>) : DcUnloadingHandleUIState<Nothing>()
}