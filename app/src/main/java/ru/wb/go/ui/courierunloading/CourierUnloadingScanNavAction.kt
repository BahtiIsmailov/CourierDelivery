package ru.wb.go.ui.courierunloading

sealed class CourierUnloadingScanNavAction {

    object NavigateToIntransit : CourierUnloadingScanNavAction()

    data class InitAndShowUnloadingItems(val items: MutableList<RemainBoxItem>) :
        CourierUnloadingScanNavAction()

    object HideUnloadingItems : CourierUnloadingScanNavAction()

}