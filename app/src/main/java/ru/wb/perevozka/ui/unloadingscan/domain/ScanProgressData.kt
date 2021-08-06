package ru.wb.perevozka.ui.unloadingscan.domain

sealed class ScanProgressData {

    object Progress : ScanProgressData()

    object Complete : ScanProgressData()

}