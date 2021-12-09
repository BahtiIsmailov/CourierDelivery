package ru.wb.go.ui.courierunloading.domain

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import ru.wb.go.db.entity.courierboxes.CourierBoxEntity
import ru.wb.go.db.entity.courierlocal.CourierOrderLocalDataEntity
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.ui.scanner.domain.ScannerState

interface CourierUnloadingInteractor {

    fun nameOffice(officeId: Int): Single<String>

    fun observeNetworkConnected(): Observable<NetworkState>

    fun readUnloadingLastBox(officeId: Int): Single<CourierUnloadingLastBoxResult>

    fun readUnloadingBoxCounter(officeId: Int): Single<CourierUnloadingBoxCounterResult>

    fun scannedBoxes(officeId: Int): Single<List<CourierBoxEntity>>

    fun observeScanProcess(officeId: Int): Observable<CourierUnloadingProcessData>

    fun removeScannedBoxes(checkedBoxes: List<String>): Completable

    fun scanLoaderProgress(): Observable<CourierUnloadingProgressData>

    fun switchScreen(): Completable

    fun scannerAction(scannerAction: ScannerState)

    fun observeOrderData(): Flowable<CourierOrderLocalDataEntity>

    fun confirmUnloading(officeId: Int): Completable

}