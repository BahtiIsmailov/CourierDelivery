package ru.wb.perevozka.network.api.app

enum class FlightStatus(val status: String) {
    ASSIGNED("assigned"),
    DCLOADING("dcloading"),
    DCUNLOADING("dcunloading"),
    UNLOADING("unloading"),
    INTRANSIT("intransit"),
    CLOSED("closed")
}