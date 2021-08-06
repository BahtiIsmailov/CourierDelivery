package ru.wb.perevozka.ui.unloadingscan

sealed class UnloadingScanSoundEvent {

    object BoxAdded : UnloadingScanSoundEvent()
    object BoxSkipAdded : UnloadingScanSoundEvent()

}