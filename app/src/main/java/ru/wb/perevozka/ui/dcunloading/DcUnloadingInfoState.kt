package ru.wb.perevozka.ui.dcunloading

sealed class DcUnloadingInfoState {

    object Empty : DcUnloadingInfoState()

    data class Complete(val barcode: String) : DcUnloadingInfoState()

//    data class Active(val barcode: String) : DcUnloadingInfoState()

    data class  Error(val barcode: String) : DcUnloadingInfoState()

}