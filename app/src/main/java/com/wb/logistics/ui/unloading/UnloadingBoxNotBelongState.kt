package com.wb.logistics.ui.unloading

sealed class UnloadingBoxNotBelongState {
    data class BelongInfo(
        val toolbarTitle: String,
        val title: String,
        val code: String,
        val address: String,
    ) :
        UnloadingBoxNotBelongState()
}