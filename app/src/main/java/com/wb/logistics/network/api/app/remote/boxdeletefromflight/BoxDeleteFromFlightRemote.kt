package com.wb.logistics.network.api.app.remote.boxdeletefromflight

data class BoxDeleteFromFlightRemote(
    val isManualInput: Boolean,
    val updatedAt: String,
    val currentOffice: DeleteCurrentOfficeRemote,
)