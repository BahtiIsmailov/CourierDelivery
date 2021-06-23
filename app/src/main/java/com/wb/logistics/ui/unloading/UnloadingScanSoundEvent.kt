package com.wb.logistics.ui.unloading

sealed class UnloadingScanSoundEvent {

    object BoxAdded : UnloadingScanSoundEvent()
    object BoxSkipAdded : UnloadingScanSoundEvent()

}