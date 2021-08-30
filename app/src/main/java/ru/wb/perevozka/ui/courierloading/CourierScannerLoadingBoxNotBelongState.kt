package ru.wb.perevozka.ui.courierloading

sealed class CourierScannerLoadingBoxNotBelongState {
    data class BelongInfo(
        val title: String,
        val code: String,
        val address: String,
        val isShowAddress: Boolean
    ) :
        CourierScannerLoadingBoxNotBelongState()
}