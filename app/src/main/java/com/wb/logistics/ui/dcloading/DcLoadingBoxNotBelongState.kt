package com.wb.logistics.ui.dcloading

sealed class DcLoadingBoxNotBelongState {
    data class BelongInfo(
        val title: String,
        val code: String,
        val address: String,
        val isShowAddress: Boolean
    ) :
        DcLoadingBoxNotBelongState()
}