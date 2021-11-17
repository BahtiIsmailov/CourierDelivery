package ru.wb.go.ui.courierorderdetails

sealed class CourierOrderDetailsProgressState {

    object Progress : CourierOrderDetailsProgressState()

    object ProgressComplete : CourierOrderDetailsProgressState()

}