package ru.wb.perevozka.ui.courierloading.domain

sealed class CourierLoadingProgressData {

    object Progress : CourierLoadingProgressData()

    object Complete : CourierLoadingProgressData()

}