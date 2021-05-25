package com.wb.logistics.ui.dcforcedtermination

sealed class DcForcedTerminationNavAction {

    object NavigateToCongratulation : DcForcedTerminationNavAction()

    object NavigateToDetails : DcForcedTerminationNavAction()

}