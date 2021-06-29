package com.wb.logistics.ui.dcunloadingforcedtermination

sealed class DcForcedTerminationState {
    data class Title(val toolbarTitle: String) : DcForcedTerminationState()
    data class BoxesUnloadCount(val countBoxes: String) : DcForcedTerminationState()
}