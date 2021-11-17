package ru.wb.go.ui.dcloading

sealed class DcLoadingScanBeepState {

    object BoxAdded : DcLoadingScanBeepState()
    object BoxSkipAdded : DcLoadingScanBeepState()

}