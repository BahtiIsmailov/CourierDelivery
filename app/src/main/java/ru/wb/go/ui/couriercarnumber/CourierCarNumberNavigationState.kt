package ru.wb.go.ui.couriercarnumber

sealed class CourierCarNumberNavigationState {

    data class NavigateToOrderDetails(val id: Int) : CourierCarNumberNavigationState()

}