package ru.wb.go.ui.unloadingscan

sealed class UnloadingScanInfoState {

    object Empty : UnloadingScanInfoState()
    data class Unloading(val barcode: String) : UnloadingScanInfoState()
    data class Return(val barcode: String) : UnloadingScanInfoState()
    data class UnloadDeny(val barcode: String) : UnloadingScanInfoState()
    data class NotInfoDeny(val barcode: String) : UnloadingScanInfoState()

}