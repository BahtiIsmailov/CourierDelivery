package ru.wb.go.ui.dcloading.domain

import ru.wb.go.db.entity.flighboxes.FlightBoxEntity
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.ui.scanner.domain.ScannerState
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface DcLoadingInteractor {

    fun barcodeManualInput(barcode: String)

    fun removeScannedBoxes(checkedBoxes: List<String>): Completable

    fun observeScanProcess(): Observable<ScanProcessData>

    fun scanLoaderProgress():  Observable<ScanProgressData>

    fun observeScannedBoxes(): Observable<List<FlightBoxEntity>>

    fun gate(): Single<String>

    fun switchScreen(): Completable

    fun scannerAction(scannerAction: ScannerState)

    fun observeNetworkConnected(): Observable<NetworkState>

}