package ru.wb.go.ui.courierdatatype


sealed class CourierDataTypeProgressState {

    object Complete : CourierDataTypeProgressState()
    object ProgressData : CourierDataTypeProgressState()

}