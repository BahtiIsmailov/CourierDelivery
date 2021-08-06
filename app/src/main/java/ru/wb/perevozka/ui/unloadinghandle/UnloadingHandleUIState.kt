package ru.wb.perevozka.ui.unloadinghandle

sealed class UnloadingHandleUIState {
    data class BoxFormatted(val number: String) : UnloadingHandleUIState()
    data class BoxAcceptDisabled(val number: String) : UnloadingHandleUIState()
    object BoxesEmpty : UnloadingHandleUIState()
    data class BoxesComplete(val boxes: List<String>) : UnloadingHandleUIState()
}