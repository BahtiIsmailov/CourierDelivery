package ru.wb.perevozka.db.entity.flighboxes

enum class BoxTracker(val status: String) {
    EMPB("NotInfoBox"),
    PBVZ("BoxNotBelongPvz"),
}