package ru.wb.perevozka.ui.dcloading

sealed class DcLoadingScanToastState {

    data class BoxAdded(val message: String) : DcLoadingScanToastState()
    data class BoxHasBeenAdded(val message: String) : DcLoadingScanToastState()

}