package com.wb.logistics.ui.unloading

sealed class UnloadingHandleUIState {
    data class BoxFormatted(val number: String) : UnloadingHandleUIState()
    data class BoxAcceptDisabled(val number: String) : UnloadingHandleUIState()
    @Deprecated("")
    object BoxesEmpty : UnloadingHandleUIState()
    data class BoxesComplete(val boxes: List<String>) : UnloadingHandleUIState()
}