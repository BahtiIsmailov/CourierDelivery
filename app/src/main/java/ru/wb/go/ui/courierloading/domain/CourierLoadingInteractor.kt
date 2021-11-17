package ru.wb.go.ui.courierloading.domain

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import ru.wb.go.db.entity.courierboxes.CourierBoxEntity
import ru.wb.go.db.entity.courierlocal.CourierLoadingInfoEntity
import ru.wb.go.db.entity.courierlocal.CourierOrderLocalDataEntity
import ru.wb.go.db.entity.courierlocal.CourierTimerEntity
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.ui.scanner.domain.ScannerState

interface CourierLoadingInteractor {

    fun observeNetworkConnected(): Observable<NetworkState>

    fun scannedBoxes(): Single<List<CourierBoxEntity>>

    fun observeScanProcess(): Observable<CourierLoadingProcessData>

    fun removeScannedBoxes(checkedBoxes: List<String>): Completable

    fun scanLoaderProgress(): Observable<CourierLoadingProgressData>

    fun switchScreen(): Completable

    fun scannerAction(scannerAction: ScannerState)

    fun observeOrderData(): Flowable<CourierOrderLocalDataEntity>

    fun deleteTask(): Completable

    fun confirmLoadingBoxes(): Single<CourierCompleteData>

    fun info(): Single<CourierLoadingInfoEntity>

}