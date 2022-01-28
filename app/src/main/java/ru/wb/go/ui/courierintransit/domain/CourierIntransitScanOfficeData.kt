package ru.wb.go.ui.courierintransit.domain

sealed class CourierIntransitScanOfficeData {

    data class NecessaryOffice(val id: Int) : CourierIntransitScanOfficeData()

    object UnknownQrOffice : CourierIntransitScanOfficeData()
    object WrongOffice : CourierIntransitScanOfficeData()

}