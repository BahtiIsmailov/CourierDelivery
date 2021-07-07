package com.wb.logistics.ui.unloading

sealed class UnloadingScanBoxState {

    data class Empty(val accepted: String) : UnloadingScanBoxState()

    data class Complete(val accepted: String) : UnloadingScanBoxState()

    data class Active(val accepted: String) : UnloadingScanBoxState()

    data class  Error(val accepted: String) : UnloadingScanBoxState()

}