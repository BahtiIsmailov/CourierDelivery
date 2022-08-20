package ru.wb.go.utils

sealed class WaitLoaderForOrder {

    object Wait : WaitLoaderForOrder()

    object Complete : WaitLoaderForOrder()
}