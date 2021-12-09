package ru.wb.go.ui.unloadingscan

sealed class UnloadingScanNavAction {
    data class NavigateToUnloadingBoxNotBelongPvz(
        val title: String,
        val description: String,
        val box: String,
        val address: String
    ) :
        UnloadingScanNavAction()

    data class NavigateToUploadedBoxes(val dstOfficeId: Int) : UnloadingScanNavAction()

    data class NavigateToHandleInput(val dstOfficeId: Int) : UnloadingScanNavAction()

    data class NavigateToReturnBoxes(val dstOfficeId: Int) : UnloadingScanNavAction()

    data class NavigateToForcedTermination(val dstOfficeId: Int) : UnloadingScanNavAction()

    object NavigateToBack : UnloadingScanNavAction()

    object NavigateToDelivery : UnloadingScanNavAction()

}