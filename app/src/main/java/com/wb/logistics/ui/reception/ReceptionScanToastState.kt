package com.wb.logistics.ui.reception

sealed class ReceptionScanToastState<out R> {

    data class BoxAdded(val message: String) : ReceptionScanToastState<Nothing>()
    data class BoxHasBeenAdded(val message: String) : ReceptionScanToastState<Nothing>()

}