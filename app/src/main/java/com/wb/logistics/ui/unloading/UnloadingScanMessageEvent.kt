package com.wb.logistics.ui.unloading

sealed class UnloadingScanMessageEvent {

    data class BoxDelivery(val message: String) : UnloadingScanMessageEvent()
    data class BoxReturned(val message: String) : UnloadingScanMessageEvent()

}