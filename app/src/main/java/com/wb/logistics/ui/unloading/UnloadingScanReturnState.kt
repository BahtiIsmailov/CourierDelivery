package com.wb.logistics.ui.unloading

sealed class UnloadingScanReturnState {

    data class ReturnBoxesEmpty(val accepted: String) : UnloadingScanReturnState()

    data class ReturnBoxesComplete(val accepted: String, val barcode: String) : UnloadingScanReturnState()

    data class ReturnBoxesActive(val accepted: String, val barcode: String) :
        UnloadingScanReturnState()

}