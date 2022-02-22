package ru.wb.go.ui.courierintransitofficescanner.domain

import io.reactivex.Observable
import ru.wb.go.db.entity.courierlocal.LocalOfficeEntity
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.ui.scanner.domain.ScannerState

interface CourierIntransitOfficeScannerInteractor {

    fun observeNetworkConnected(): Observable<NetworkState>

    fun getOffices(): Observable<List<LocalOfficeEntity>>

    fun observeOfficeIdScanProcess(): Observable<CourierIntransitOfficeScanData>

    fun scannerAction(scannerAction: ScannerState)

}