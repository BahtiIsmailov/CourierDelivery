package ru.wb.go.network.api.app.remote.warehousematchingboxes

data class WarehouseMatchingBoxResponse(
    val barcode: String,
    val srcOffice: WarehouseMatchingSrcOfficeRemote,
    val dstOffice: WarehouseMatchingDstOfficeRemote,
    val smID: Int,
)