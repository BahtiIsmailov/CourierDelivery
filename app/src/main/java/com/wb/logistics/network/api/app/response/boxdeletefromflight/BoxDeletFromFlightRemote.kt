package com.wb.logistics.network.api.app.response.boxdeletefromflight

data class BoxDeletFromFlightRemote(
    val isManualInput: Boolean,
    val currentOffice: DeleteCurrentOfficeRemote,
)