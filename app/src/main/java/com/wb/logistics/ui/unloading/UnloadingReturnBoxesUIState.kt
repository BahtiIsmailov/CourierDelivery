package com.wb.logistics.ui.unloading

sealed class UnloadingReturnBoxesUIState<out R> {

    data class ReceptionBoxesItem(val items : List<UnloadingReturnBoxesItem>) : UnloadingReturnBoxesUIState<Nothing>()

    object Empty : UnloadingReturnBoxesUIState<Nothing>()

    object Progress : UnloadingReturnBoxesUIState<Nothing>()

    object ProgressComplete : UnloadingReturnBoxesUIState<Nothing>()

}