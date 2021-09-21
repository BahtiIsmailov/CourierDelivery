package ru.wb.perevozka.ui.courierunloading.domain

sealed class CourierUnloadingProgressData {

    object Progress : CourierUnloadingProgressData()

    object Complete : CourierUnloadingProgressData()

}