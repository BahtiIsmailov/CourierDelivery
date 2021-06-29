package com.wb.logistics.ui.dcunloadingforcedtermination

sealed class DcForcedTerminationNavAction {

    object NavigateToCongratulation : DcForcedTerminationNavAction()

    object NavigateToDetails : DcForcedTerminationNavAction()

}