package com.wb.logistics.ui.dcloading.domain

sealed class ScanBoxData {

    data class BoxHasBeenAdded(val barcode: String, val gate: String) : ScanBoxData()

    data class BoxAdded(val barcode: String, val gate: String) : ScanBoxData()

    data class BoxDoesNotBelongGate(
        val barcode: String,
        val gate: String,
        val accepted: String,
    ) : ScanBoxData()

    data class BoxDoesNotBelongDc(val barcode: String, val address: String, val gate: String) :
        ScanBoxData()

    class BoxDoesNotBelongFlight(
        val barcode: String,
        val address: String,
        val gate: String,
    ) : ScanBoxData()

    class BoxDoesNotBelongInfo(val barcode: String) : ScanBoxData()

    object Empty : ScanBoxData()

}