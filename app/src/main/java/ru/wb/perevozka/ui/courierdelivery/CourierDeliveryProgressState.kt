package ru.wb.perevozka.ui.courierdelivery

sealed class CourierDeliveryProgressState {

    object Progress : CourierDeliveryProgressState()

    object ProgressComplete : CourierDeliveryProgressState()

}