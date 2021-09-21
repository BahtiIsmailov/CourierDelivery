package ru.wb.perevozka.ui.courierintransit.domain

import io.reactivex.Flowable
import io.reactivex.Observable
import ru.wb.perevozka.db.entity.courierboxes.CourierIntransitGroupByOfficeEntity
import ru.wb.perevozka.ui.scanner.domain.ScannerState

interface CourierIntransitInteractor {

    fun observeBoxesGroupByOffice(): Flowable<List<CourierIntransitGroupByOfficeEntity>>

    fun observeOfficeIdScanProcess(): Observable<Int>

    fun scannerAction(scannerAction: ScannerState)

    fun startTime(): Observable<Long>

}