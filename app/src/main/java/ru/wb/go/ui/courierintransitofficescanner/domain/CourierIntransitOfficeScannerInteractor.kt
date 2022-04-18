package ru.wb.go.ui.courierintransitofficescanner.domain

import io.reactivex.Observable
import ru.wb.go.db.entity.courierlocal.LocalOfficeEntity
import ru.wb.go.ui.BaseServiceInteractor
import ru.wb.go.ui.scanner.domain.ScannerState

interface CourierIntransitOfficeScannerInteractor: BaseServiceInteractor {

    fun getOffices(): Observable<List<LocalOfficeEntity>>

    fun observeOfficeIdScanProcess(): Observable<CourierIntransitOfficeScanData>

    fun scannerAction(scannerAction: ScannerState)

}