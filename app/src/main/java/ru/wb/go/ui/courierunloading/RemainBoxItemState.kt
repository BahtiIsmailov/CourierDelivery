package ru.wb.go.ui.courierunloading

sealed class RemainBoxItemState {

    data class InitItems(val items: MutableList<RemainBoxItem>) :
        RemainBoxItemState()

    data class UpdateItems(val index: Int, val items: MutableList<RemainBoxItem>) :
        RemainBoxItemState()

    data class Empty(val info: String) : RemainBoxItemState()

}
