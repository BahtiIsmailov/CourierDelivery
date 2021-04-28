package com.wb.logistics.ui.unloading

sealed class UnloadingHandleUIState<out R> {
    data class BoxFormatted(val number: String) : UnloadingHandleUIState<Nothing>()
    data class BoxAcceptDisabled(val number: String) : UnloadingHandleUIState<Nothing>()
    object BoxesEmpty : UnloadingHandleUIState<Nothing>()
    data class BoxesComplete(val boxes: List<String>) : UnloadingHandleUIState<Nothing>()
}