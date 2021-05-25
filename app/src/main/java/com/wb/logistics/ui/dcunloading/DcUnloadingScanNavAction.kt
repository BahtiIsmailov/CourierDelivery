package com.wb.logistics.ui.dcunloading

sealed class DcUnloadingScanNavAction {

    object NavigateToDcUploadedBoxes : DcUnloadingScanNavAction()

    object NavigateToDcHandleInput : DcUnloadingScanNavAction()

    data class NavigateToUnloadingBoxNotBelongDc(val toolbarTitle: String) :
        DcUnloadingScanNavAction()

    object NavigateToDcCongratulation : DcUnloadingScanNavAction()

    object NavigateToDcForcedTermination : DcUnloadingScanNavAction()

    object NavigateToBack : DcUnloadingScanNavAction()

}