package ru.wb.go.ui.courierdata

sealed class CourierDataUIAction {

    data class TextChange(val text: String, val type: CourierDataQueryType) : CourierDataUIAction()

    data class CompleteClick(var userData: MutableList<CourierData>) : CourierDataUIAction()

}