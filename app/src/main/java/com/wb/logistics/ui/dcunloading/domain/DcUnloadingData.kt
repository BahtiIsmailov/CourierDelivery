package com.wb.logistics.ui.dcunloading.domain

sealed class DcUnloadingData {

    data class BoxAlreadyUnloaded(val barcode: String) : DcUnloadingData()

    data class BoxUnload(val barcode: String) : DcUnloadingData()

    object BoxDoesNotBelongDc : DcUnloadingData()

}