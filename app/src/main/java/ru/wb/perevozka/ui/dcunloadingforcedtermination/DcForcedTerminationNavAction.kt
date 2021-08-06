package ru.wb.perevozka.ui.dcunloadingforcedtermination

sealed class DcForcedTerminationNavAction {

    object NavigateToCongratulation : DcForcedTerminationNavAction()

    object NavigateToDetails : DcForcedTerminationNavAction()

}