package ru.wb.perevozka.ui.courierunloading

sealed class CourierUnloadingScanProgress {

    object LoaderProgress : CourierUnloadingScanProgress()

    object LoaderComplete : CourierUnloadingScanProgress()

}