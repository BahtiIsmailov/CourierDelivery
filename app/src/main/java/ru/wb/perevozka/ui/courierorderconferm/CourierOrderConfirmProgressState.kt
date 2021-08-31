package ru.wb.perevozka.ui.courierorderconferm

sealed class CourierOrderConfirmProgressState {

    object Progress : CourierOrderConfirmProgressState()

    object ProgressComplete : CourierOrderConfirmProgressState()

}