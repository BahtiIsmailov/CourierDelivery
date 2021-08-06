package ru.wb.perevozka.ui.unloadingscan

sealed class UnloadingScanMessageEvent {

    data class BoxDelivery(val message: String) : UnloadingScanMessageEvent()
    data class BoxReturned(val message: String) : UnloadingScanMessageEvent()

}