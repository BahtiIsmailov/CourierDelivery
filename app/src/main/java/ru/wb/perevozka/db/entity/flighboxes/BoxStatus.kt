package ru.wb.perevozka.db.entity.flighboxes

enum class BoxStatus(val status: String) {
    UNDEFINED("Undefined"),
    TAKE_ON_BALANCE("TakenOnBalance"),
    REMOVED_FROM_BALANCE("RemovedFromBalance"),
    TAKE_ON_FLIGHT("TakenOnFlight"),
    REMOVED_FROM_FLIGHT("RemovedFromFlight"),
    DELIVERED("Delivered")
}