package ru.wb.perevozka.ui.courierdelivery

import ru.wb.perevozka.ui.courierorderdetails.CourierOrderDetailsItem

sealed class CourierDeliveryUIState {

    data class InitItems(val items: MutableList<CourierOrderDetailsItem>) :
        CourierDeliveryUIState()

    object Empty : CourierDeliveryUIState()

}