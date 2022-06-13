package ru.wb.go.ui.courierloading.domain

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import ru.wb.go.db.entity.courierlocal.CourierOrderLocalDataEntity
import ru.wb.go.db.entity.courierlocal.LocalBoxEntity
import ru.wb.go.ui.BaseServiceInteractor
import ru.wb.go.ui.scanner.domain.ScannerAction
import ru.wb.go.ui.scanner.domain.ScannerState

interface CourierLoadingInteractor : BaseServiceInteractor {

    suspend fun scannedBoxes():  List<LocalBoxEntity>

    suspend fun observeScanProcess( ):  CourierLoadingProcessData

    suspend fun scanLoaderProgress(): CourierLoadingProgressData

    fun scannerAction(scannerAction: ScannerState)

    suspend fun observeOrderData(): CourierOrderLocalDataEntity

    suspend fun deleteTask()

    suspend fun confirmLoadingBoxes():  CourierCompleteData

    suspend fun getGate():  String

    suspend fun loadingBoxBoxesGroupByOffice():LoadingBoxGoals

}