package com.wb.logistics.ui.dcunloading.domain

sealed class ScanProgressData {

    object Progress : ScanProgressData()

    object Complete : ScanProgressData()

}