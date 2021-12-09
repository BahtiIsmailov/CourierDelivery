package ru.wb.go.db.entity.flighboxes

enum class LoadStatus(val status: String) {
    UNDEFINED("Undefined"),
    TAKE_ON_BALANCE("TakenOnBalance"),
    REMOVED_FROM_BALANCE("RemovedFromBalance"),
    TAKE_ON_FLIGHT("TakenOnFlight"),
    REMOVED_FROM_FLIGHT("RemovedFromFlight"),
    DELIVERED("Delivered")
}