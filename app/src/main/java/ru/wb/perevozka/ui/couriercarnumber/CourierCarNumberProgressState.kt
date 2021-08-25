package ru.wb.perevozka.ui.couriercarnumber

sealed class CourierCarNumberProgressState {

    object Progress : CourierCarNumberProgressState()

    object ProgressComplete : CourierCarNumberProgressState()

}