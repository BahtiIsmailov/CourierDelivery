package com.wb.logistics.ui.unloading

sealed class UnloadingBoxesState {
    data class Title(val toolbarTitle: String) : UnloadingBoxesState()
    object BoxesEmpty : UnloadingBoxesState()
    data class BoxesComplete(val boxes: List<String>) : UnloadingBoxesState()
}