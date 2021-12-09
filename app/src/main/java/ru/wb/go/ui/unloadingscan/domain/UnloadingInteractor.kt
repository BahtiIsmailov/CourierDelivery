package ru.wb.go.ui.unloadingscan.domain

import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.ui.scanner.domain.ScannerState
import io.reactivex.Observable
import io.reactivex.Single

interface UnloadingInteractor {

    fun barcodeManualInput(barcode: String)

    fun observeUnloadingProcess(currentOfficeId: Int): Observable<UnloadingData>

    fun scanLoaderProgress(): Observable<ScanProgressData>

    fun observeCountUnloadReturnedBoxAndSwitchScreen(currentOfficeId: Int): Observable<Int>

    fun scannerAction(scannerAction: ScannerState)

    fun isUnloadingComplete(currentOfficeId: Int): Single<Boolean>

    fun officeNameById(currentOfficeId: Int): Single<String>

    fun observeNetworkConnected(): Observable<NetworkState>

}