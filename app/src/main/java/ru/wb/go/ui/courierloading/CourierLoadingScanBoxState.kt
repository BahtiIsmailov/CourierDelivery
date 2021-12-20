package ru.wb.go.ui.courierloading

sealed class CourierLoadingScanBoxState {

    object InitScanner : CourierLoadingScanBoxState()

    object LoadInCar : CourierLoadingScanBoxState()

    object NotRecognizedQrWithTimer : CourierLoadingScanBoxState()

    object NotRecognizedQr : CourierLoadingScanBoxState()

    object ForbiddenTakeWithTimer : CourierLoadingScanBoxState()

    object ForbiddenTakeBox : CourierLoadingScanBoxState()

}