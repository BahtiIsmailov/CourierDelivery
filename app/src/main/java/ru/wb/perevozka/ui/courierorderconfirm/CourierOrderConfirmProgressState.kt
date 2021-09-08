package ru.wb.perevozka.ui.courierorderconfirm

sealed class CourierOrderConfirmProgressState {

    object Progress : CourierOrderConfirmProgressState()

    object ProgressComplete : CourierOrderConfirmProgressState()

}