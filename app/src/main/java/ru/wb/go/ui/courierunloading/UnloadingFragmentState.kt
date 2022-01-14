package ru.wb.go.ui.courierunloading

sealed class UnloadingFragmentState {

    data class Empty(val data: UnloadingFragmentData) : UnloadingFragmentState()

    data class BoxInit(val data: UnloadingFragmentData) : UnloadingFragmentState()

    data class BoxAdded(val data: UnloadingFragmentData) : UnloadingFragmentState()

    data class UnknownQr(val data: UnloadingFragmentData) : UnloadingFragmentState()
    data class ForbiddenBox(val data: UnloadingFragmentData) : UnloadingFragmentState()
    data class WrongBox(val data: UnloadingFragmentData) : UnloadingFragmentState()

    data class ScannerReady(val data: UnloadingFragmentData) : UnloadingFragmentState()

}

data class UnloadingFragmentData(
    val status: String,
    val qrCode: String,
    val address: String,
    val accepted: String,
)