package com.wb.logistics.ui.unloadingscan.domain

sealed class UnloadingAction {

    data class BoxUnloadAdded(val barcode: String) : UnloadingAction()

    data class BoxReturnAdded(val barcode: String) : UnloadingAction()

    data class BoxDoesNotBelongPvz(val barcode: String, val address: String) : UnloadingAction()

    data class BoxWasUnloadedAnotherPvz(val barcode: String, val address: String) : UnloadingAction()

    data class BoxInfoEmpty(val barcode: String) : UnloadingAction()

    object Init : UnloadingAction()

}