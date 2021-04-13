package com.wb.logistics.ui.reception

sealed class ReceptionBoxesUIState<out R> {

    data class ReceptionBoxesItem(val items : List<com.wb.logistics.ui.reception.ReceptionBoxesItem>) : ReceptionBoxesUIState<Nothing>()

    object Empty : ReceptionBoxesUIState<Nothing>()

    object Progress : ReceptionBoxesUIState<Nothing>()

    object ProgressComplete : ReceptionBoxesUIState<Nothing>()

}