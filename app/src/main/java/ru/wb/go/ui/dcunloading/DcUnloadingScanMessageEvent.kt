package ru.wb.go.ui.dcunloading

sealed class DcUnloadingScanMessageEvent {

    data class BoxAdded(val message: String) : DcUnloadingScanMessageEvent()
    data class BoxAlreadyUnloaded(val message: String) : DcUnloadingScanMessageEvent()

}