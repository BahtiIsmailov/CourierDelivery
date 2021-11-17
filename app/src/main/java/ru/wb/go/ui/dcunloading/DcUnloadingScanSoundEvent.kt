package ru.wb.go.ui.dcunloading

sealed class DcUnloadingScanSoundEvent {

    object BoxAdded : DcUnloadingScanSoundEvent()
    object BoxSkipAdded : DcUnloadingScanSoundEvent()

}