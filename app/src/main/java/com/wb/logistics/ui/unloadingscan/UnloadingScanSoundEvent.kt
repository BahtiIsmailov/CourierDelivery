package com.wb.logistics.ui.unloadingscan

sealed class UnloadingScanSoundEvent {

    object BoxAdded : UnloadingScanSoundEvent()
    object BoxSkipAdded : UnloadingScanSoundEvent()

}