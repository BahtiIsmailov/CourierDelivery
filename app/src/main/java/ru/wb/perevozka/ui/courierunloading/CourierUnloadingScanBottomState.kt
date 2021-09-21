package ru.wb.perevozka.ui.courierunloading

sealed class CourierUnloadingScanBottomState {

    object Enable : CourierUnloadingScanBottomState()
    object Progress : CourierUnloadingScanBottomState()
    object Disable : CourierUnloadingScanBottomState()

}