package com.wb.logistics.ui.unloadingscan.domain

sealed class ScanProgressData {

    object Progress : ScanProgressData()

    object Complete : ScanProgressData()

}