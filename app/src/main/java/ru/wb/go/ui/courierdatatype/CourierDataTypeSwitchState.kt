package ru.wb.go.ui.courierdatatype


sealed class CourierDataTypeSwitchState {

    object IsSelfEmployed : CourierDataTypeSwitchState()
    object IsIP : CourierDataTypeSwitchState()

}