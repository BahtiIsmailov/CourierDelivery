package com.wb.logistics.network.api.app.entity.boxinfo

data class BoxInfoEntity(
    val barcode: String,
    val srcOffice: BoxInfoSrcOfficeEntity,
    val dstOffice: BoxInfoDstOfficeEntity,
    val smID: Int,
)