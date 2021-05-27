package com.wb.logistics.ui.unloading

sealed class UnloadingBoxesState {
    object BoxesEmpty : UnloadingBoxesState()
    data class BoxesComplete(val boxes: List<String>) : UnloadingBoxesState()
}