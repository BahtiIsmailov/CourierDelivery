package com.wb.logistics.ui.reception

sealed class ReceptionBoxesUIState<out R> {

    data class ReceptionBoxesItem(val items : List<ReceptionBoxItem>) : ReceptionBoxesUIState<Nothing>()

    object Empty : ReceptionBoxesUIState<Nothing>()

}