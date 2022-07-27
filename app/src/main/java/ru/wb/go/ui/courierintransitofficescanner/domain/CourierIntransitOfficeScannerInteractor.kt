package ru.wb.go.ui.courierintransitofficescanner.domain

import kotlinx.coroutines.flow.Flow
import ru.wb.go.db.entity.courierlocal.LocalOfficeEntity
import ru.wb.go.ui.BaseServiceInteractor
import ru.wb.go.ui.scanner.domain.ScannerState

interface CourierIntransitOfficeScannerInteractor: BaseServiceInteractor {

      fun getOffices(): Flow<List<LocalOfficeEntity>>

    fun observeOfficeIdScanProcess(): Flow<CourierIntransitOfficeScanData>

    fun scannerAction(scannerAction: ScannerState)

    fun clearMutableSharedFlow()

}