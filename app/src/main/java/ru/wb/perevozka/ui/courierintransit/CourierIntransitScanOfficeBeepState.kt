package ru.wb.perevozka.ui.courierintransit

sealed class CourierIntransitScanOfficeBeepState {

    object Office : CourierIntransitScanOfficeBeepState()

    object UnknownOffice : CourierIntransitScanOfficeBeepState()

}