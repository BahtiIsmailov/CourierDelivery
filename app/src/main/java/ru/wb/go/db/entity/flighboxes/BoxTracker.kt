package ru.wb.go.db.entity.flighboxes

enum class BoxTracker(val status: String) {
    EMPB("NotInfoBox"),
    PBVZ("BoxNotBelongPvz"),
}