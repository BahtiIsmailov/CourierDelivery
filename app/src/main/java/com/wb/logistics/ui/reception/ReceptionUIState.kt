package com.wb.logistics.ui.reception

sealed class ReceptionUIState<out R> {
    data class NavigateToReceptionBoxNotBelong(val title: String, val box: String, val address: String) :
        ReceptionUIState<Nothing>()

    object NavigateToBoxes : ReceptionUIState<Nothing>()
    object Empty : ReceptionUIState<Nothing>()
}