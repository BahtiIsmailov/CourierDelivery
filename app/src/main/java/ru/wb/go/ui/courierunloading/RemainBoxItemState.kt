package ru.wb.go.ui.courierunloading

sealed class RemainBoxItemState {

    data class InitItems(val items: MutableList<String>) :
        RemainBoxItemState()

    data class UpdateItems(val index: Int, val items: MutableList<String>) :
        RemainBoxItemState()

    data class Empty(val info: String) : RemainBoxItemState()

}
