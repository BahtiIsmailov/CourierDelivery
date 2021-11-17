package ru.wb.go.ui.dcunloading.domain

import ru.wb.go.db.entity.dcunloadedboxes.DcReturnHandleBarcodeEntity
import ru.wb.go.db.entity.dcunloadedboxes.DcUnloadingBarcodeEntity
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.ui.dcloading.domain.ScanProgressData
import ru.wb.go.ui.scanner.domain.ScannerState
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface DcUnloadingInteractor {

    fun barcodeManualInput(barcode: String)

    fun observeUnloadingProcess(): Observable<DcUnloadingData>

    fun scanLoaderProgress():  Observable<ScanProgressData>

    fun findDcUnloadedHandleBoxes(): Single<List<DcReturnHandleBarcodeEntity>>

    fun findDcUnloadedListBoxes(): Single<List<DcUnloadingBarcodeEntity>>

    fun isBoxesUnloaded(): Single<Boolean>

    fun scannerAction(scannerAction: ScannerState)

    fun switchScreenToClosed(): Completable

    fun observeNetworkConnected(): Observable<NetworkState>

}