package ru.wb.go.ui.courierunloading.domain

import android.util.Log
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import ru.wb.go.app.AppPreffsKeys
import ru.wb.go.app.AppPreffsKeys.OFFICE_NUMBER_UNLOADING
import ru.wb.go.db.CourierLocalRepository
import ru.wb.go.db.entity.courierlocal.CourierOrderLocalDataEntity
import ru.wb.go.db.entity.courierlocal.LocalBoxEntity
import ru.wb.go.db.entity.courierlocal.LocalOfficeEntity
import ru.wb.go.network.api.app.AppRemoteRepository
import ru.wb.go.network.monitor.NetworkMonitorRepository
import ru.wb.go.ui.BaseServiceInteractorImpl
import ru.wb.go.ui.scanner.domain.ScannerAction
import ru.wb.go.ui.scanner.domain.ScannerRepository
import ru.wb.go.ui.scanner.domain.ScannerState
import ru.wb.go.utils.managers.DeviceManager
import ru.wb.go.utils.managers.TimeManager
import ru.wb.go.utils.prefs.SharedWorker

class CourierUnloadingInteractorImpl(
    networkMonitorRepository: NetworkMonitorRepository,
    deviceManager: DeviceManager,
    private val remoteRepo: AppRemoteRepository,
    private val sharedWorker: SharedWorker,
    private val scannerRepo: ScannerRepository,
    private val timeManager: TimeManager,
    private val localRepo: CourierLocalRepository,
) : BaseServiceInteractorImpl(networkMonitorRepository, deviceManager),
    CourierUnloadingInteractor {

    companion object {
        const val EMPTY_ADDRESS = ""
    }

    @OptIn(FlowPreview::class)
    override fun observeScanProcess(officeId: Int): Flow<CourierUnloadingProcessData> {
        return scannerRepo.observeScannerAction()
            .filter { it is ScannerAction.ScanResult }
            .map { it as ScannerAction.ScanResult }
            .map { scannerRepo.parseScanBoxQr(it.value) }
            .flatMapMerge { parsedScan ->
                val boxes = localRepo.getBoxes()
                val box = boxes.find { box -> box.boxId == parsedScan.boxId }
                val scanTime = timeManager.getLocalTime()
                val result: CourierUnloadingScanBoxData
                when {
                    !parsedScan.isOk -> {
                        result = CourierUnloadingScanBoxData.UnknownQr
                        scannerRepo.scannerState(ScannerState.HoldScanUnknown)
                    }
                    box == null -> {
                        result = CourierUnloadingScanBoxData.ForbiddenBox(
                            parsedScan.boxId,
                            EMPTY_ADDRESS
                        )
                        scannerRepo.scannerState(ScannerState.HoldScanError)
                    }
                    parsedScan.officeId != officeId.toString() ||
                            // пикнули коробку с дублированием boxId но другим офисом
                            box.officeId.toString() != parsedScan.officeId -> {
                        result = CourierUnloadingScanBoxData.WrongBox(
                            parsedScan.boxId,
                            EMPTY_ADDRESS,
                        )
                        localRepo.takeBackBox(box)
                        val failedOffice = sharedWorker.load(OFFICE_NUMBER_UNLOADING,0)
                        if (localRepo.isBoxesExist(box.boxId).isNotEmpty()) {
                            localRepo.setFailedBoxes(
                                failedOffice,
                                timeManager.getLocalTime(),
                                box.boxId,
                                parsedScan.officeId.toInt()
                            )
                        }
//                        localRepo.getOfflineBoxes()
//                        Log.e("boxesObserver","$boxes")
                        scannerRepo.scannerState(ScannerState.HoldScanError)
                    }
                    else -> {
                        val boxOut = box.copy(deliveredAt = scanTime)
                        result =
                            CourierUnloadingScanBoxData.BoxAdded(parsedScan.boxId, boxOut.address)
                        localRepo.unloadBox(boxOut)
                        scannerRepo.scannerState(ScannerState.HoldScanComplete)
                    }
                }
                val currentOffice = localRepo.findOfficeById(officeId)
                flowOf(CourierUnloadingProcessData(result, currentOffice.deliveredBoxes, currentOffice.countBoxes))
            }
    }


    override suspend fun scannerRepoHoldStart() {
        scannerRepo.holdStart()
    }


    override fun clearMutableSharedFlow() {
        scannerRepo.clearScannerState()
    }

    var scanLoaderProgressSubject = MutableSharedFlow<CourierUnloadingProgressData>()
    override suspend fun getCurrentOffice(officeId: Int): LocalOfficeEntity {
        return localRepo.findOfficeById(officeId)

    }

    override fun scanLoaderProgress(): Flow<CourierUnloadingProgressData> {
        return scanLoaderProgressSubject
    }

    override suspend fun removeScannedBoxes(checkedBoxes: List<String>) {
        return
    }

    override fun scannerAction(scannerAction: ScannerState) {
        scannerRepo.scannerState(scannerAction)
    }

    override fun observeOrderData(): Flow<CourierOrderLocalDataEntity> {
        return localRepo.observeOrderData()
    }

    override suspend fun completeOfficeUnload() {
        val srcOfficeId = localRepo.getSrcOfficeId()
        val boxes = localRepo.getOfflineBoxes()
        Log.e("boxes","$boxes") // сюда уже приходит пустые элементы а нужно их заполнять до


        remoteRepo.setIntransitTask(localRepo.getOrderId(), boxes, srcOfficeId ?: 0)//1
        localRepo.setOnlineOffices()
    }

    override suspend fun getRemainBoxes(officeId: Int): List<LocalBoxEntity> {
        return localRepo.getRemainBoxes(officeId)
    }

    override suspend fun getOrderId(): String {
        // FIXME: У одного курьера здесь происходит NullPointerException. Причина пока не понятна
        return localRepo.getOrder()?.orderId.toString()
    }

}

