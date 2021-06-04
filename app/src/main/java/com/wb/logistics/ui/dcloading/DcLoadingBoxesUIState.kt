package com.wb.logistics.ui.dcloading

sealed class DcLoadingBoxesUIState {

    data class ReceptionBoxesItem(val items: MutableList<DcLoadingBoxesItem>) :
        DcLoadingBoxesUIState()

    data class ReceptionBoxItem(val index: Int, val item: DcLoadingBoxesItem) :
        DcLoadingBoxesUIState()

    object Empty : DcLoadingBoxesUIState()

    object Progress : DcLoadingBoxesUIState()

    object ProgressComplete : DcLoadingBoxesUIState()

}