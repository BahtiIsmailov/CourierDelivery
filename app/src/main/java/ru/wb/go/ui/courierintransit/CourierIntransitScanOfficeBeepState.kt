package ru.wb.go.ui.courierintransit

sealed class CourierIntransitScanOfficeBeepState {

    object Office : CourierIntransitScanOfficeBeepState()

    object UnknownQrOffice : CourierIntransitScanOfficeBeepState()
    object WrongOffice : CourierIntransitScanOfficeBeepState()

}