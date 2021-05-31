package com.wb.logistics.ui.dcloading

sealed class DcLoadingBoxesUIState {

    data class ReceptionBoxesItem(val items : List<DcLoadingBoxesItem>) : DcLoadingBoxesUIState()

    object Empty : DcLoadingBoxesUIState()

    object Progress : DcLoadingBoxesUIState()

    object ProgressComplete : DcLoadingBoxesUIState()

}