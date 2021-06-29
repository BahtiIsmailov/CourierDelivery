package com.wb.logistics.ui.dcunloadingforcedtermination

sealed class DcForcedTerminationDetailsState {
    data class Title(val toolbarTitle: String) : DcForcedTerminationDetailsState()
    data class BoxesComplete(val boxes: List<DcForcedTerminationDetailsItem>) :
        DcForcedTerminationDetailsState()
}