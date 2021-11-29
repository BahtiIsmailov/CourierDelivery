package ru.wb.go.ui.courierordertimer

sealed class CourierOrderTimerNavigationState {

    object NavigateToWarehouse: CourierOrderTimerNavigationState()

    object NavigateToScanner: CourierOrderTimerNavigationState()


}
