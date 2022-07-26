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

    fun observeOrderData(): Flow<CourierOrderLocalDataEntity>

    suspend fun deleteTask()

    suspend fun confirmLoadingBoxes(): CourierCompleteData

    suspend fun confirmLoadingBoxesEveryFiveMinutes()

    suspend fun getGate(): String?

    fun clearScannerState()

    suspend fun loadingBoxBoxesGroupByOffice(): LoadingBoxGoals

    suspend fun scanRepoHoldStart()

}

