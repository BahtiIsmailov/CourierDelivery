package ru.wb.perevozka.ui.courierorderdetails

sealed class CourierOrderDetailsProgressState {

    object Progress : CourierOrderDetailsProgressState()

    object ProgressComplete : CourierOrderDetailsProgressState()

}