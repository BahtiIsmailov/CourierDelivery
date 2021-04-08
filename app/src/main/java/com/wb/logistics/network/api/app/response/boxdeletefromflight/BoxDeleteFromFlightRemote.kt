package com.wb.logistics.network.api.app.response.boxdeletefromflight

data class BoxDeleteFromFlightRemote(
    val isManualInput: Boolean,
    val currentOffice: DeleteCurrentOfficeRemote,
)