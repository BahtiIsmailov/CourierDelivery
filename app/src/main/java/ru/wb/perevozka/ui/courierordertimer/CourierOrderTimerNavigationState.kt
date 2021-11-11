package ru.wb.perevozka.ui.courierordertimer

sealed class CourierOrderTimerNavigationState {

    object NavigateToWarehouse: CourierOrderTimerNavigationState()

    object NavigateToScanner: CourierOrderTimerNavigationState()


}
