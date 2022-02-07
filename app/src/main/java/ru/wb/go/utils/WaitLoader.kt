package ru.wb.go.utils

sealed class WaitLoader{
    object Wait : WaitLoader()

    object Complete : WaitLoader()
}
