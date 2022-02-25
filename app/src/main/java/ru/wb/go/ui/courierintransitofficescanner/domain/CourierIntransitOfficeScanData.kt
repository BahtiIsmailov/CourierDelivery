package ru.wb.go.ui.courierintransitofficescanner.domain

sealed class CourierIntransitOfficeScanData {

    data class NecessaryOfficeScan(val id: Int) : CourierIntransitOfficeScanData()

    object UnknownQrOfficeScan : CourierIntransitOfficeScanData()

    object HoldSplashUnlock : CourierIntransitOfficeScanData()

    object HoldSplashLock : CourierIntransitOfficeScanData()

    object WrongOfficeScan : CourierIntransitOfficeScanData()

}