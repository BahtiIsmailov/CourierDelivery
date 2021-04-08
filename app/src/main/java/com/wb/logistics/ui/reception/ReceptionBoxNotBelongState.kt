package com.wb.logistics.ui.reception

sealed class ReceptionBoxNotBelongState {
    data class BelongInfo(
        val toolbarTitle: String,
        val title: String,
        val code: String,
        val address: String,
    ) :
        ReceptionBoxNotBelongState()
}