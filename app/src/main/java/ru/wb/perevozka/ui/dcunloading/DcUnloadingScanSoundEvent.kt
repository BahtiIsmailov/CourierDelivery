package ru.wb.perevozka.ui.dcunloading

sealed class DcUnloadingScanSoundEvent {

    object BoxAdded : DcUnloadingScanSoundEvent()
    object BoxSkipAdded : DcUnloadingScanSoundEvent()

}