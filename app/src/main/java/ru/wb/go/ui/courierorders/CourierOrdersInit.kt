package ru.wb.go.ui.courierorders

sealed class CourierOrdersInit {

    object Orders : CourierOrdersInit()
    object Details : CourierOrdersInit()

}
