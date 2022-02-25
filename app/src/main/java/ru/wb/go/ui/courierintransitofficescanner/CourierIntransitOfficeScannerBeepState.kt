package ru.wb.go.ui.courierintransitofficescanner

sealed class CourierIntransitOfficeScannerBeepState {

    object Office : CourierIntransitOfficeScannerBeepState()

    object UnknownQrOffice : CourierIntransitOfficeScannerBeepState()

    object WrongOffice : CourierIntransitOfficeScannerBeepState()

}