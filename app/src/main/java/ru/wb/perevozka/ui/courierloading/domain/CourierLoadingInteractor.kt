package ru.wb.perevozka.ui.courierloading.domain

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import ru.wb.perevozka.db.entity.courierboxes.CourierBoxEntity
import ru.wb.perevozka.db.entity.courierlocal.CourierOrderLocalDataEntity
import ru.wb.perevozka.network.monitor.NetworkState
import ru.wb.perevozka.ui.scanner.domain.ScannerAction

interface CourierLoadingInteractor {

    fun observeNetworkConnected(): Observable<NetworkState>

    fun scannedBoxes(): Single<List<CourierBoxEntity>>

    fun observeScanProcess(): Observable<CourierLoadingProcessData>

    fun removeScannedBoxes(checkedBoxes: List<String>): Completable

    fun scanLoaderProgress(): Observable<CourierLoadingProgressData>

    fun switchScreen(): Completable

    fun scannerAction(scannerAction: ScannerAction)

    fun observeOrderData(): Flowable<CourierOrderLocalDataEntity>

}