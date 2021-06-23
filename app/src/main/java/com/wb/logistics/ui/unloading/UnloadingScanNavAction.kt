package com.wb.logistics.ui.unloading

sealed class UnloadingScanNavAction {
    data class NavigateToUnloadingBoxNotBelongPoint(
        val title: String,
        val description: String,
        val box: String,
        val address: String,
    ) :
        UnloadingScanNavAction()

    data class NavigateToUploadedBoxes(val dstOfficeId: Int) : UnloadingScanNavAction()

    data class NavigateToHandleInput(val dstOfficeId: Int) : UnloadingScanNavAction()

    data class NavigateToReturnBoxes(val dstOfficeId: Int) : UnloadingScanNavAction()

    data class NavigateToForcedTermination(val dstOfficeId: Int) : UnloadingScanNavAction()

    object NavigateToBack : UnloadingScanNavAction()

}