package ru.wb.perevozka.ui.courierloading

sealed class CourierLoadingScanProgress {

    object LoaderProgress : CourierLoadingScanProgress()

    object LoaderComplete : CourierLoadingScanProgress()

}