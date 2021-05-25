package com.wb.logistics.ui.dcforcedtermination

sealed class DcForcedTerminationState {
    data class Title(val toolbarTitle: String) : DcForcedTerminationState()
    data class BoxesUnloadCount(val countBoxes: String) : DcForcedTerminationState()
}