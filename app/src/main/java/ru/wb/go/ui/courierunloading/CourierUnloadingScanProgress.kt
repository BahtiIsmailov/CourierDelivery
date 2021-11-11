package ru.wb.go.ui.courierunloading

sealed class CourierUnloadingScanProgress {

    object LoaderProgress : CourierUnloadingScanProgress()

    object LoaderComplete : CourierUnloadingScanProgress()

}