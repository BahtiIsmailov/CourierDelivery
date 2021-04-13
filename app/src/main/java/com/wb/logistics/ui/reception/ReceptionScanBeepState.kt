package com.wb.logistics.ui.reception

sealed class ReceptionScanBeepState<out R> {

    object BoxAdded : ReceptionScanBeepState<Nothing>()
    object BoxSkipAdded : ReceptionScanBeepState<Nothing>()

}