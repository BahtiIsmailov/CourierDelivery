package ru.wb.go.ui.courierdata

sealed class CourierDataUIAction {

    data class FocusChange(
        val text: String,
        val type: CourierDataQueryType,
        val hasFocus: Boolean
    ) :
        CourierDataUIAction()

    data class TextChange(val text: String, val type: CourierDataQueryType) : CourierDataUIAction()

    data class CompleteClick(var userData: MutableList<CourierData>) : CourierDataUIAction()

}