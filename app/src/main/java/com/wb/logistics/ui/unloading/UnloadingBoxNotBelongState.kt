package com.wb.logistics.ui.unloading

sealed class UnloadingBoxNotBelongState {
    data class BelongInfo(
        val title: String,
        val description: String,
        val code: String,
        val address: String,
        val isShowAddress: Boolean,
    ) :
        UnloadingBoxNotBelongState()
}