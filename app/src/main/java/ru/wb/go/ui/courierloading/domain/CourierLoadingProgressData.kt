package ru.wb.go.ui.courierloading.domain

sealed class CourierLoadingProgressData {

    object Progress : CourierLoadingProgressData()

    object Complete : CourierLoadingProgressData()

}