package com.wb.logistics.ui.unloading

sealed class UnloadingScanSoundEvent<out R> {

    object BoxAdded : UnloadingScanSoundEvent<Nothing>()
    object BoxSkipAdded : UnloadingScanSoundEvent<Nothing>()

}