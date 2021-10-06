package ru.wb.perevozka.ui.courierorderdetails

sealed class CourierOrderDetailsUIState {

    data class InitItems(val items: MutableList<CourierOrderDetailsItem>) :
        CourierOrderDetailsUIState()

    data class UpdateItems(val index: Int, val items: MutableList<CourierOrderDetailsItem>) :
        CourierOrderDetailsUIState()

    object Empty : CourierOrderDetailsUIState()

}