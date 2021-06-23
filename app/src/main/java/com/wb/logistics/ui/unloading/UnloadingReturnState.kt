package com.wb.logistics.ui.unloading

sealed class UnloadingReturnState {

    data class ReturnBoxesEmpty(val accepted: String) : UnloadingReturnState()

    data class ReturnBoxesComplete(val accepted: String, val barcode: String) : UnloadingReturnState()

    data class ReturnBoxesActive(val accepted: String, val barcode: String) :
        UnloadingReturnState()

}