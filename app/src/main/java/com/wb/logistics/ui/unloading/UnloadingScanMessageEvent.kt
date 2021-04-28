package com.wb.logistics.ui.unloading

sealed class UnloadingScanMessageEvent<out R> {

    data class BoxAdded(val message: String) : UnloadingScanMessageEvent<Nothing>()
    data class BoxHasBeenAdded(val message: String) : UnloadingScanMessageEvent<Nothing>()

}