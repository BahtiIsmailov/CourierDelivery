package com.wb.logistics.ui.unloading

sealed class UnloadingScanBoxState {

    data class UnloadedBoxesEmpty(val accepted: String) : UnloadingScanBoxState()

    data class UnloadedBoxesComplete(val accepted: String) : UnloadingScanBoxState()

    data class UnloadedBoxesActive(val accepted: String, val barcode: String) :
        UnloadingScanBoxState()

}