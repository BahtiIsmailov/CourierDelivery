package ru.wb.go.ui.unloadingscan

sealed class UnloadingScanSoundEvent {

    object BoxAdded : UnloadingScanSoundEvent()
    object BoxSkipAdded : UnloadingScanSoundEvent()

}