package ru.wb.go.ui.courierunloading.domain

import io.reactivex.*
import ru.wb.go.db.entity.courierlocal.CourierOrderLocalDataEntity
import ru.wb.go.db.entity.courierlocal.LocalBoxEntity
import ru.wb.go.db.entity.courierlocal.LocalOfficeEntity
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.ui.scanner.domain.ScannerState

interface CourierUnloadingInteractor {

    fun getCurrentOffice(officeId: Int): Single<LocalOfficeEntity>

    fun observeNetworkConnected(): Observable<NetworkState>

    fun observeScanProcess(officeId: Int): Observable<CourierUnloadingProcessData>

    fun removeScannedBoxes(checkedBoxes: List<String>): Completable

    fun scanLoaderProgress(): Observable<CourierUnloadingProgressData>

    fun scannerAction(scannerAction: ScannerState)

    fun observeOrderData(): Flowable<CourierOrderLocalDataEntity>

    fun completeOfficeUnload(): Completable

    fun getRemainBoxes(officeId: Int):Maybe<List<LocalBoxEntity>>
}