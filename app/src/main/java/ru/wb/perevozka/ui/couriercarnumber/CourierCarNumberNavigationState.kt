package ru.wb.perevozka.ui.couriercarnumber

sealed class CourierCarNumberNavigationState {

    data class NavigateToTimer(val title: String) :
        CourierCarNumberNavigationState()

    data class NavigateToDialogInfo(
        val type: Int,
        val title: String,
        val message: String,
        val button: String
    ) : CourierCarNumberNavigationState()
}