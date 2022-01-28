package ru.wb.go.db.entity.courierlocal

data class LocalComplexOrderEntity(
    val order: LocalOrderEntity,
    val offices: List<LocalOfficeEntity>,
)