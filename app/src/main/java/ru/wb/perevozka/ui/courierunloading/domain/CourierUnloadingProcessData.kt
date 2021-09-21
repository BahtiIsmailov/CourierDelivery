package ru.wb.perevozka.ui.courierunloading.domain

data class CourierUnloadingProcessData(
    val scanBoxData: CourierUnloadingScanBoxData,
    val unloadingCounter: Int,
    val fromCounter: Int
)
