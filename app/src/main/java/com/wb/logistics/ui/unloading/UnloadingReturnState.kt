package com.wb.logistics.ui.unloading

sealed class UnloadingReturnState<out R> {

    data class ReturnBoxesEmpty(val accepted: String) : UnloadingReturnState<Nothing>()

    data class ReturnBoxesComplete(val accepted: String, val barcode: String) : UnloadingReturnState<Nothing>()

    data class ReturnBoxesActive(val accepted: String, val barcode: String) :
        UnloadingReturnState<Nothing>()

}