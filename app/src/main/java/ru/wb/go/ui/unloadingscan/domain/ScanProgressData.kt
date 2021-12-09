package ru.wb.go.ui.unloadingscan.domain

sealed class ScanProgressData {

    object Progress : ScanProgressData()

    object Complete : ScanProgressData()

}