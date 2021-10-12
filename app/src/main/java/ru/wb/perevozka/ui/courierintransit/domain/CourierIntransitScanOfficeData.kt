package ru.wb.perevozka.ui.courierintransit.domain

sealed class CourierIntransitScanOfficeData {

    data class Office(val id: Int) : CourierIntransitScanOfficeData()

    object UnknownOffice : CourierIntransitScanOfficeData()

}