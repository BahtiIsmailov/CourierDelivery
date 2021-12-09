package ru.wb.go.ui.courierorderconfirm

sealed class CourierOrderConfirmProgressState {

    object Progress : CourierOrderConfirmProgressState()

    object ProgressComplete : CourierOrderConfirmProgressState()

}