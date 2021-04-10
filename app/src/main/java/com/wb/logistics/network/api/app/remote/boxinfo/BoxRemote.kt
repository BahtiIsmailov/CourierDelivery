package com.wb.logistics.network.api.app.remote.boxinfo

data class BoxRemote(
    val barcode: String,
    val srcOffice: SrcOfficeRemote,
    val dstOffice: DstOfficeRemote,
    val smID: Int,
)