package com.wb.logistics.ui.dcloading.domain

sealed class ScanBoxData {

    data class BoxHasBeenAdded(val barcode: String, val gate: String) : ScanBoxData()

    data class BoxAdded(val barcode: String, val gate: String) : ScanBoxData()

    data class BoxDoesNotBelongDc(val barcode: String, val address: String) : ScanBoxData()

    class BoxDoesNotBelongFlight(val barcode: String, val address: String) : ScanBoxData()

    class BoxDoesNotBelongInfoEmpty(val barcode: String) : ScanBoxData()

    object Empty : ScanBoxData()

}