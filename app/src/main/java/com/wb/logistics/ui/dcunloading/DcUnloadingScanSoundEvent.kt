package com.wb.logistics.ui.dcunloading

sealed class DcUnloadingScanSoundEvent {

    object BoxAdded : DcUnloadingScanSoundEvent()
    object BoxSkipAdded : DcUnloadingScanSoundEvent()

}