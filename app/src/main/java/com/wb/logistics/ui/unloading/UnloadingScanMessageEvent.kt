package com.wb.logistics.ui.unloading

sealed class UnloadingScanMessageEvent<out R> {

    data class BoxDelivery(val message: String) : UnloadingScanMessageEvent<Nothing>()
    data class BoxReturned(val message: String) : UnloadingScanMessageEvent<Nothing>()

}