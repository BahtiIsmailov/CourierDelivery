package ru.wb.go.ui.courierunloading.domain

sealed class CourierUnloadingProgressData {

    object Progress : CourierUnloadingProgressData()

    object Complete : CourierUnloadingProgressData()

}