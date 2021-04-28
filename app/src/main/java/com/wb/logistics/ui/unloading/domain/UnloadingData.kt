package com.wb.logistics.ui.unloading.domain

sealed class UnloadingData {

    data class BoxAlreadyUnloaded(val barcode: String) : UnloadingData()

    data class BoxAlreadyReturn(val barcode: String) : UnloadingData()

    data class BoxUnloadAdded(val barcode: String) : UnloadingData()

    data class BoxReturnAdded(val barcode: String) : UnloadingData()

    data class BoxDoesNotBelongPoint(val barcode: String, val address: String) : UnloadingData()

    data class BoxSaveRemoteError(val message: String) : UnloadingData()

    object Empty : UnloadingData()

}