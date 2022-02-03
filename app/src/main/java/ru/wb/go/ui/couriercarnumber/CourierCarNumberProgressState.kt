package ru.wb.go.ui.couriercarnumber

sealed class CourierCarNumberProgressState {

    object Progress : CourierCarNumberProgressState()

    object ProgressComplete : CourierCarNumberProgressState()

}