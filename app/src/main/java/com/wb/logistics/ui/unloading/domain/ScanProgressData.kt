package com.wb.logistics.ui.unloading.domain

sealed class ScanProgressData {

    object Progress : ScanProgressData()

    object Complete : ScanProgressData()

}