package com.wb.logistics.ui.dcunloading

sealed class DcUnloadedState {

    data class Empty(val accepted: String) : DcUnloadedState()

    data class Complete(val accepted: String) : DcUnloadedState()

    data class Active(val accepted: String) : DcUnloadedState()

    data class  Error(val accepted: String) : DcUnloadedState()

}