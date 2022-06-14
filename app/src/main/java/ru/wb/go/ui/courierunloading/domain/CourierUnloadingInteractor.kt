package ru.wb.go.ui.courierunloading.domain

import io.reactivex.*
import ru.wb.go.db.entity.courierlocal.CourierOrderLocalDataEntity
import ru.wb.go.db.entity.courierlocal.LocalBoxEntity
import ru.wb.go.db.entity.courierlocal.LocalOfficeEntity
import ru.wb.go.ui.BaseServiceInteractor
import ru.wb.go.ui.scanner.domain.ScannerState

interface CourierUnloadingInteractor : BaseServiceInteractor {

    suspend fun getCurrentOffice(officeId: Int):  LocalOfficeEntity

    suspend fun observeScanProcess(officeId: Int):  CourierUnloadingProcessData

    suspend fun removeScannedBoxes(checkedBoxes: List<String>)

    suspend fun scanLoaderProgress(): CourierUnloadingProgressData

    suspend fun scannerAction(scannerAction: ScannerState)

    suspend fun observeOrderData():  CourierOrderLocalDataEntity

    suspend fun completeOfficeUnload()

    suspend fun getRemainBoxes(officeId: Int):  List<LocalBoxEntity>

    suspend fun getOrderId(): String
}