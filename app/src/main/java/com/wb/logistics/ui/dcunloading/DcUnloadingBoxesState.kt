package com.wb.logistics.ui.dcunloading

sealed class DcUnloadingBoxesState {
    data class Title(val toolbarTitle: String) : DcUnloadingBoxesState()
    object BoxesEmpty : DcUnloadingBoxesState()
    data class BoxesComplete(val boxes: List<DcUnloadingBoxesItem>) : DcUnloadingBoxesState()
}