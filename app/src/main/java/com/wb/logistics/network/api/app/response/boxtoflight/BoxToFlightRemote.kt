package com.wb.logistics.network.api.app.response.boxtoflight

data class BoxToFlightRemote(
    val barcode: String,
    val isManualInput: Boolean,
    val dstOffice: CurrentOfficeRemote,
)