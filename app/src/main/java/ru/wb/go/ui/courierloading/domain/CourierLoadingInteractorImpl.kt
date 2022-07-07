package ru.wb.go.ui.courierloading.domain

import android.util.Log
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import ru.wb.go.db.CourierLocalRepository
import ru.wb.go.db.TaskTimerRepository
import ru.wb.go.db.entity.courierlocal.CourierOrderLocalDataEntity
import ru.wb.go.db.entity.courierlocal.LocalBoxEntity
import ru.wb.go.db.entity.courierlocal.LocalLoadingBoxEntity
import ru.wb.go.network.api.app.AppRemoteRepository
import ru.wb.go.network.monitor.NetworkMonitorRepository
import ru.wb.go.ui.BaseServiceInteractorImpl
import ru.wb.go.ui.scanner.domain.ScannerAction
import ru.wb.go.ui.scanner.domain.ScannerRepository
import ru.wb.go.ui.scanner.domain.ScannerState
import ru.wb.go.utils.managers.DeviceManager
import ru.wb.go.utils.managers.TimeManager

class CourierLoadingInteractorImpl(
    networkMonitorRepository: NetworkMonitorRepository,
    deviceManager: DeviceManager,
    private val remoteRepo: AppRemoteRepository,
    private val scanRepo: ScannerRepository,
    private val timeManager: TimeManager,
    private val localRepo: CourierLocalRepository,
    private val taskTimerRepository: TaskTimerRepository,
) : BaseServiceInteractorImpl(networkMonitorRepository, deviceManager),
    CourierLoadingInteractor {


    private val scanLoaderProgressSubject = MutableSharedFlow<CourierLoadingProgressData>(
        extraBufferCapacity = Int.MAX_VALUE,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    companion object {
        const val DELAY_HOLD_SCANNER = 1500L
    }

    override suspend fun scannedBoxes(): List<LocalBoxEntity> {
        return localRepo.readAllLoadingBoxesSync()
    }

    private fun scanResult(
        scannerState: ScannerState,
        data: CourierLoadingScanBoxData,
        boxCount: Int
    ): Flow<CourierLoadingProcessData> {
        scanRepo.scannerState(scannerState)
        return flowOf(CourierLoadingProcessData(data, boxCount))
//            .onEach{
//                scanRepo.holdStart()
//            }
            .onEach {
                CourierLoadingProcessData(
                    CourierLoadingScanBoxData.ScannerReady,
                    boxCount
                )
            }

    }

    override suspend fun scanRepoHoldStart(){
        scanRepo.holdStart()
    }



     @OptIn(FlowPreview::class)
    override fun observeScanProcess(): Flow<CourierLoadingProcessData> {
        return scanRepo.observeScannerAction()
            .filter { it is ScannerAction.ScanResult }
            .map { it as ScannerAction.ScanResult }
            .map { scanRepo.parseScanBoxQr(it.value) }
            .flatMapMerge { parsedScan ->
                val boxes = localRepo.getBoxes()
                val scanTime = timeManager.getLocalTime()
                if (!parsedScan.isOk) {
                    scanResult(
                        ScannerState.HoldScanUnknown,
                        CourierLoadingScanBoxData.NotRecognizedQr,
                        boxes.size
                    )
                } else {
                    val offices = localRepo.getOffices()
                    val office =
                        offices.find { off -> off.officeId.toString() == parsedScan.officeId }
                    var box = boxes.find { b -> b.boxId == parsedScan.boxId }
                    if (office == null || (box != null && box.officeId.toString() != parsedScan.officeId)) {
                        scanResult(
                            ScannerState.HoldScanError,
                            CourierLoadingScanBoxData.ForbiddenTakeBox(parsedScan.boxId),
                            boxes.size
                        )
                    } else {
                        var isNewBox = false
                        if (box == null) {
                            isNewBox = true
                            box = LocalBoxEntity(
                                boxId = parsedScan.boxId, officeId = office.officeId,
                                address = office.address, loadingAt = scanTime, deliveredAt = ""
                            )
                        } else {
                            box = box.copy(loadingAt = scanTime)
                        }

                        qrComplete(box, boxes.size, isNewBox, scanTime)

                    }
                }
            }
    }


    private suspend fun qrComplete(
        box: LocalBoxEntity,
        countBox: Int,
        isNewBox: Boolean,
        scanTime: String
    ): Flow<CourierLoadingProcessData> {
        return when (countBox) {
            0 -> {
                firstBoxLoaderProgress()
                val orderId = localRepo.getOrderId()
                try {
                    remoteRepo.setStartTask(orderId, box)
                }finally {
                    firstBoxLoaderComplete()
                }
                localRepo.loadBoxOnboard(box, true)
                taskTimerRepository.stopTimer()
                localRepo.setOrderOrderStart(scanTime)
                Log.e("UniqueId","FirstBoxAdded")
                scanResult(
                    ScannerState.HoldScanComplete,
                    CourierLoadingScanBoxData.FirstBoxAdded(
                        box.boxId,
                        box.address
                    ),
                    1
                )
            }
            else -> {
                localRepo.loadBoxOnboard(box, isNewBox)
                scanResult(
                    ScannerState.HoldScanComplete,
                    CourierLoadingScanBoxData.SecondaryBoxAdded(box.boxId, box.address),
                    when (isNewBox) {
                        true -> countBox + 1
                        else -> countBox
                    }
                )
            }
        }
    }



    private suspend fun firstBoxLoaderProgress() {
        scanLoaderProgressSubject.emit(CourierLoadingProgressData.Progress)
    }

    private fun firstBoxLoaderComplete() {
        scanLoaderProgressSubject.tryEmit(CourierLoadingProgressData.Complete)
    }

    override fun scanLoaderProgress(): Flow<CourierLoadingProgressData> {
        return scanLoaderProgressSubject
    }

    override fun scannerAction(scannerAction: ScannerState) {
        scanRepo.scannerState(scannerAction)
    }

    override fun observeOrderData(): Flow<CourierOrderLocalDataEntity> {
        return localRepo.observeOrderData()

    }

    override suspend fun deleteTask() {
        taskTimerRepository.stopTimer()
        val it = localRepo.getOrderId()
        remoteRepo.deleteTask(it)
        localRepo.deleteOrder()

    }

    override suspend fun confirmLoadingBoxes(): CourierCompleteData {
        val one = localRepo.readAllLoadingBoxesSync()
        val two = localRepo.getOrderId()
        val res = remoteRepo.setReadyTask(two, one)
        localRepo.setOrderAfterLoadStatus(res.coast)
        return CourierCompleteData(res.coast, one.size)

    }


    override suspend fun getGate(): String? {
        return localRepo.getOrderGate()
    }

    override suspend fun loadingBoxBoxesGroupByOffice(): LoadingBoxGoals {
        val it = localRepo.loadingBoxBoxesGroupByOffice()
        var pvzCount = 0
        var boxCount = 0
        val localLoadingBoxEntities = mutableListOf<LocalLoadingBoxEntity>()
        it.forEach { localLoadingBox ->
            pvzCount++
            boxCount += localLoadingBox.count
            localLoadingBoxEntities.add(localLoadingBox)
        }
        return LoadingBoxGoals(pvzCount, boxCount, localLoadingBoxEntities)
    }
}



data class LoadingBoxGoals(
    var pvzCount: Int,
    var boxCount: Int,
    var localLoadingBoxEntity: MutableList<LocalLoadingBoxEntity>
)

