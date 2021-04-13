package com.wb.logistics.network.api.app.remote.matchingboxes

data class MatchingBoxRemote(
    val barcode: String,
    val srcOffice: MatchingSrcOfficeRemote,
    val dstOffice: MatchingDstOfficeRemote,
    val smID: Int,
)