package com.wb.logistics.network.api.app.remote.warehousematchingboxes

data class WarehouseMatchingBoxRemote(
    val barcode: String,
    val srcOffice: WarehouseMatchingSrcOfficeRemote,
    val dstOffice: WarehouseMatchingDstOfficeRemote,
    val smID: Int,
)