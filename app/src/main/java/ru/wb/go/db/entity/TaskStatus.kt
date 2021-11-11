package ru.wb.go.db.entity

enum class TaskStatus(val status: String) {
    EMPTY(""),
    TIMER("timer"),
    STARTED("started"),
    INTRANSIT("intransit"),
}