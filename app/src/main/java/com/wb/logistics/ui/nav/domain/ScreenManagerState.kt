package com.wb.logistics.ui.nav.domain

sealed class ScreenManagerState(val id: ScreenId) {

    object Flight : ScreenManagerState(ScreenId.FLIGHT)
    object FlightDelivery : ScreenManagerState(ScreenId.FLIGHT_DELIVERY)
    object FlightPickUpPoint : ScreenManagerState(ScreenId.FLIGHT_PIK_UP_POINT)
    object ReceptionScan : ScreenManagerState(ScreenId.RECEPTION_SCAN)
    data class Unloading(val officeId: Int, val shortAddress: String) :
        ScreenManagerState(ScreenId.UNLOADING)

    enum class ScreenId { FLIGHT, FLIGHT_DELIVERY, FLIGHT_PIK_UP_POINT, RECEPTION_SCAN, UNLOADING }

}