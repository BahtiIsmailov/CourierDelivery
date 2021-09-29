package ru.wb.perevozka.ui.courierintransit.domain

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import ru.wb.perevozka.db.entity.courierboxes.CourierIntransitGroupByOfficeEntity
import ru.wb.perevozka.ui.scanner.domain.ScannerState

interface CourierIntransitInteractor {

    fun observeBoxesGroupByOffice(): Observable<List<CourierIntransitGroupByOfficeEntity>>

    fun observeOfficeIdScanProcess(): Observable<Int>

    fun scannerAction(scannerAction: ScannerState)

    fun startTime(): Observable<Long>

    fun completeDelivery(): Single<CompleteDeliveryResult>

}