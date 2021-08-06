package ru.wb.perevozka.ui.unloadingboxes

sealed class UnloadingBoxesState {
    object BoxesEmpty : UnloadingBoxesState()
    data class BoxesComplete(val boxes: MutableList<UnloadingBoxesItem>) : UnloadingBoxesState()
}