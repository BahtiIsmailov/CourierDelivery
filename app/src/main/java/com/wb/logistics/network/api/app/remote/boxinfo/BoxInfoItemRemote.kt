package com.wb.logistics.network.api.app.remote.boxinfo

data class BoxInfoItemRemote(
    val barcode: String,
    val srcOffice: BoxInfoSrcOfficeRemote,
    val dstOffice: BoxInfoDstOfficeRemote,
    val smID: Int,
)