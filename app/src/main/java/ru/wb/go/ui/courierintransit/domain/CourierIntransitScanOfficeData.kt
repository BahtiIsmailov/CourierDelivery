package ru.wb.go.ui.courierintransit.domain

sealed class CourierIntransitScanOfficeData {

    data class Office(val id: Int) : CourierIntransitScanOfficeData()

    object UnknownOffice : CourierIntransitScanOfficeData()

}