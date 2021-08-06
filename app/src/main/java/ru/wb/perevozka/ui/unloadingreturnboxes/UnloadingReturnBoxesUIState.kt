package ru.wb.perevozka.ui.unloadingreturnboxes

sealed class UnloadingReturnBoxesUIState {

    data class ReceptionBoxesItem(val items: MutableList<UnloadingReturnBoxesItem>) :
        UnloadingReturnBoxesUIState()

    data class ReceptionBoxItem(val index: Int, val item: UnloadingReturnBoxesItem) :
        UnloadingReturnBoxesUIState()

    object Empty : UnloadingReturnBoxesUIState()

    object Progress : UnloadingReturnBoxesUIState()

    object ProgressComplete : UnloadingReturnBoxesUIState()

}