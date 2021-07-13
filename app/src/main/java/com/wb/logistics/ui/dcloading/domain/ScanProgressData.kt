package com.wb.logistics.ui.dcloading.domain

sealed class ScanProgressData {

    object Progress : ScanProgressData()

    object Complete : ScanProgressData()

}