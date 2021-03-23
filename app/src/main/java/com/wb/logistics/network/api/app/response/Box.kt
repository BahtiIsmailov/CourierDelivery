package com.wb.logistics.network.api.app.response

import com.google.gson.annotations.SerializedName

data class Box(
    @SerializedName("dc") val dc: Dc_,
    @SerializedName("dstOffice") val dstOffice: DstOffice,
    @SerializedName("barcode") val barcode: String,
)


