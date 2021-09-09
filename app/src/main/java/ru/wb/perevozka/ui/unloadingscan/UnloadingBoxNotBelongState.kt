package ru.wb.perevozka.ui.unloadingscan

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