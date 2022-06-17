package ru.wb.go.ui.courierloading.domain

import kotlinx.coroutines.flow.Flow
import ru.wb.go.db.entity.courierlocal.CourierOrderLocalDataEntity
import ru.wb.go.db.entity.courierlocal.LocalBoxEntity
import ru.wb.go.ui.BaseServiceInteractor
import ru.wb.go.ui.scanner.domain.ScannerState

interface CourierLoadingInteractor : BaseServiceInteractor {

    suspend fun scannedBoxes(): List<LocalBoxEntity>

    fun observeScanProcess(): Flow<CourierLoadingProcessData>

    fun scanLoaderProgress(): Flow<CourierLoadingProgressData>

    fun scannerAction(scannerAction: ScannerState)

    suspend fun observeOrderData(): CourierOrderLocalDataEntity

    suspend fun deleteTask()

    suspend fun confirmLoadingBoxes(): CourierCompleteData

    suspend fun getGate(): String

    suspend fun loadingBoxBoxesGroupByOffice(): LoadingBoxGoals

}