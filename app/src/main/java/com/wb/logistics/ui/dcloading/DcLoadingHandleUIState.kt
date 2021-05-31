package com.wb.logistics.ui.dcloading

sealed class DcLoadingHandleUIState {
    data class BoxFormatted(val number: String) : DcLoadingHandleUIState()
    data class BoxAcceptDisabled(val number: String) : DcLoadingHandleUIState()
}