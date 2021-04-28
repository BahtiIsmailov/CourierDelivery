package com.wb.logistics.ui.unloading

sealed class UnloadingScanBoxState<out R> {

    data class UnloadedBoxesEmpty(val accepted: String) : UnloadingScanBoxState<Nothing>()

    data class UnloadedBoxesComplete(val accepted: String) : UnloadingScanBoxState<Nothing>()

    data class UnloadedBoxesActive(val accepted: String, val barcode: String) :
        UnloadingScanBoxState<Nothing>()

}