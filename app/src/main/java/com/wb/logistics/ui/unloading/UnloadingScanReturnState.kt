package com.wb.logistics.ui.unloading

sealed class UnloadingScanReturnState {

    data class Empty(val accepted: String) : UnloadingScanReturnState()

    data class Complete(val accepted: String) : UnloadingScanReturnState()

    data class Active(val accepted: String) : UnloadingScanReturnState()

    data class  Error(val accepted: String) : UnloadingScanReturnState()

}