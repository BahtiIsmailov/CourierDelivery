package ru.wb.go.ui.couriermap

sealed class CourierVisibilityManagerBar {

    object Visible : CourierVisibilityManagerBar()

    object Hide : CourierVisibilityManagerBar()

}