package ru.wb.go.ui.courierunloading.domain

import kotlinx.coroutines.flow.Flow
import ru.wb.go.db.entity.courierlocal.CourierOrderLocalDataEntity
import ru.wb.go.db.entity.courierlocal.LocalBoxEntity
import ru.wb.go.db.entity.courierlocal.LocalOfficeEntity
import ru.wb.go.ui.BaseServiceInteractor
import ru.wb.go.ui.scanner.domain.ScannerState

interface CourierUnloadingInteractor : BaseServiceInteractor {

    suspend fun getCurrentOffice(officeId: Int):  LocalOfficeEntity

    fun observeScanProcess(officeId: Int): Flow<CourierUnloadingProcessData>

    suspend fun removeScannedBoxes(checkedBoxes: List<String>)

    fun scanLoaderProgress(): Flow<CourierUnloadingProgressData>

    fun scannerAction(scannerAction: ScannerState)

    fun observeOrderData(): Flow<CourierOrderLocalDataEntity>

    suspend fun completeOfficeUnload()

    suspend fun getRemainBoxes(officeId: Int):  List<LocalBoxEntity>

    suspend fun getOrderId(): String
}
