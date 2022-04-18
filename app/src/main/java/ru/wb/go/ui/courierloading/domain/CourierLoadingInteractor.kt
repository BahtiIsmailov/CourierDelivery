package ru.wb.go.ui.courierloading.domain

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import ru.wb.go.db.entity.courierlocal.CourierOrderLocalDataEntity
import ru.wb.go.db.entity.courierlocal.LocalBoxEntity
import ru.wb.go.ui.BaseServiceInteractor
import ru.wb.go.ui.scanner.domain.ScannerState

interface CourierLoadingInteractor : BaseServiceInteractor {

    fun scannedBoxes(): Single<List<LocalBoxEntity>>

    fun observeScanProcess(): Observable<CourierLoadingProcessData>

    fun scanLoaderProgress(): Observable<CourierLoadingProgressData>

    fun scannerAction(scannerAction: ScannerState)

    fun observeOrderData(): Flowable<CourierOrderLocalDataEntity>

    fun deleteTask(): Completable

    fun confirmLoadingBoxes(): Single<CourierCompleteData>

    fun getGate(): Single<String>

}