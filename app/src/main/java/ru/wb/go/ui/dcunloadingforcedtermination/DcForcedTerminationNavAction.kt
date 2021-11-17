package ru.wb.go.ui.dcunloadingforcedtermination

sealed class DcForcedTerminationNavAction {

    object NavigateToCongratulation : DcForcedTerminationNavAction()

    object NavigateToDetails : DcForcedTerminationNavAction()

}