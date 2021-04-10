package com.wb.logistics.network.api.app.remote.boxdeletefromflight

data class BoxDeleteFromFlightRemote(
    val isManualInput: Boolean,
    val currentOffice: DeleteCurrentOfficeRemote,
)