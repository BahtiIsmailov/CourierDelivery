package ru.wb.go.ui.courierintransitofficescanner.domain

import io.reactivex.Observable
import kotlinx.coroutines.flow.Flow
import ru.wb.go.db.entity.courierlocal.LocalOfficeEntity
import ru.wb.go.ui.BaseServiceInteractor
import ru.wb.go.ui.scanner.domain.ScannerAction
import ru.wb.go.ui.scanner.domain.ScannerState

interface CourierIntransitOfficeScannerInteractor: BaseServiceInteractor {

    suspend fun getOffices(): List<LocalOfficeEntity>

    suspend  fun observeOfficeIdScanProcess():  CourierIntransitOfficeScanData

      fun scannerAction(scannerAction: ScannerState)

}