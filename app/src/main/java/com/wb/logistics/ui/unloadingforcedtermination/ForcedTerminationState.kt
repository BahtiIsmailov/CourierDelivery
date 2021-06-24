package com.wb.logistics.ui.unloadingforcedtermination

sealed class ForcedTerminationState {
    data class Title(val toolbarTitle: String) : ForcedTerminationState()
    object BoxesEmpty : ForcedTerminationState()
    data class BoxesComplete(val title: String, val boxes: List<ForcedTerminationItem>) :
        ForcedTerminationState()
}