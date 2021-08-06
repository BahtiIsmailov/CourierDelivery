package ru.wb.perevozka.ui.unloadingscan

sealed class UnloadingScanErrorState {

    data class BoxInfoEmpty(val barcode: String) : UnloadingScanErrorState()

    data class BoxDoesNotBelongPvz(val barcode: String) : UnloadingScanErrorState()

}