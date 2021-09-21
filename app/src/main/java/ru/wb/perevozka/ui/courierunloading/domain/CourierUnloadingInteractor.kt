package ru.wb.perevozka.ui.courierunloading.domain

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import ru.wb.perevozka.db.entity.courierboxes.CourierBoxEntity
import ru.wb.perevozka.db.entity.courierlocal.CourierOrderLocalDataEntity
import ru.wb.perevozka.network.monitor.NetworkState
import ru.wb.perevozka.ui.scanner.domain.ScannerState

interface CourierUnloadingInteractor {

    fun nameOffice(officeId: Int): Single<String>

    fun observeNetworkConnected(): Observable<NetworkState>

    fun readInitLastUnloadingBox(officeId: Int): Single<CourierUnloadingInitResult>

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