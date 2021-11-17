package ru.wb.go.ui.courierintransit

sealed class CourierIntransitScanOfficeBeepState {

    object Office : CourierIntransitScanOfficeBeepState()

    object UnknownOffice : CourierIntransitScanOfficeBeepState()

}