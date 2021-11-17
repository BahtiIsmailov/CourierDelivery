package ru.wb.go.ui.courierloading

sealed class CourierLoadingScanProgress {

    object LoaderProgress : CourierLoadingScanProgress()

    object LoaderComplete : CourierLoadingScanProgress()

}