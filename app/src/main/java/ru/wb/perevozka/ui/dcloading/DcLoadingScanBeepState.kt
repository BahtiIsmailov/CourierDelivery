package ru.wb.perevozka.ui.dcloading

sealed class DcLoadingScanBeepState {

    object BoxAdded : DcLoadingScanBeepState()
    object BoxSkipAdded : DcLoadingScanBeepState()

}