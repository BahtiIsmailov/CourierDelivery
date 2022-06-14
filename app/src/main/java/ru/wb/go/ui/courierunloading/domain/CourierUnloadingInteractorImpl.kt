package ru.wb.go.ui.courierunloading.domain

import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import ru.wb.go.db.CourierLocalRepository
import ru.wb.go.db.entity.courierlocal.CourierOrderLocalDataEntity
import ru.wb.go.db.entity.courierlocal.LocalBoxEntity
import ru.wb.go.db.entity.courierlocal.LocalOfficeEntity
import ru.wb.go.network.api.app.AppRemoteRepository
import ru.wb.go.network.monitor.NetworkMonitorRepository
import ru.wb.go.network.rx.RxSchedulerFactory
import ru.wb.go.ui.BaseServiceInteractorImpl
import ru.wb.go.ui.scanner.domain.ScannerAction
import ru.wb.go.ui.scanner.domain.ScannerRepository
import ru.wb.go.ui.scanner.domain.ScannerState
import ru.wb.go.utils.managers.DeviceManager
import ru.wb.go.utils.managers.TimeManager

class CourierUnloadingInteractorImpl(
    rxSchedulerFactory: RxSchedulerFactory,
    networkMonitorRepository: NetworkMonitorRepository,
    deviceManager: DeviceManager,
    private val remoteRepo: AppRemoteRepository,
    private val scannerRepo: ScannerRepository,
    private val timeManager: TimeManager,
    private val localRepo: CourierLocalRepository,
) : BaseServiceInteractorImpl(rxSchedulerFactory, networkMonitorRepository, deviceManager),
    CourierUnloadingInteractor {

    companion object {
        const val EMPTY_ADDRESS = ""
    }

    override suspend fun observeScanProcess(officeId: Int): CourierUnloadingProcessData {
        var result: CourierUnloadingScanBoxData? = null
        return withContext(Dispatchers.IO) {
            scannerRepo.observeScannerAction().onEach {
                if (it is ScannerAction.ScanResult) {
                    val parsedScan = scannerRepo.parseScanBoxQr(it.value)
                    val boxes = localRepo.getBoxes()
                    val box = boxes.find { box -> box.boxId == parsedScan.boxId }
                    val scanTime = timeManager.getLocalTime()
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
                                // сложный случай. пикнули коробку с дублированием boxId но другим офисом
                                box.officeId.toString() != parsedScan.officeId -> {
                            result = CourierUnloadingScanBoxData.WrongBox(
                                parsedScan.boxId,
                                EMPTY_ADDRESS
                            )
                            localRepo.takeBackBox(box)
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
                }
            }

            val res = localRepo.findOfficeById(officeId)
            scannerRepo.holdStart()
            CourierUnloadingProcessData(result!!, res.deliveredBoxes, res.countBoxes)
        }
    }


//    override fun observeScanProcess(officeId: Int): Observable<CourierUnloadingProcessData> {
//        return scannerRepo.observeScannerAction()
//            .filter { it is ScannerAction.ScanResult }
//            .map { it as ScannerAction.ScanResult }
//            .map { scannerRepo.parseScanBoxQr(it.value) }
//            .flatMap { parsedScan ->
//                val boxes = localRepo.getBoxes()
//                val box = boxes.find { box -> box.boxId == parsedScan.boxId }
//                val scanTime = timeManager.getLocalTime()
//                val result: CourierUnloadingScanBoxData
//                when {
//                    !parsedScan.isOk -> {
//                        result = CourierUnloadingScanBoxData.UnknownQr
//                        scannerRepo.scannerState(ScannerState.HoldScanUnknown)
//                    }
//                    box == null -> {
//                        result = CourierUnloadingScanBoxData.ForbiddenBox(
//                            parsedScan.boxId,
//                            EMPTY_ADDRESS
//                        )
//                        scannerRepo.scannerState(ScannerState.HoldScanError)
//                    }
//                    parsedScan.officeId != officeId.toString() ||
//                            // сложный случай. пикнули коробку с дублированием boxId но другим офисом
//                            box.officeId.toString() != parsedScan.officeId -> {
//                        result = CourierUnloadingScanBoxData.WrongBox(
//                            parsedScan.boxId,
//                            EMPTY_ADDRESS
//                        )
//                        localRepo.takeBackBox(box)
//                        scannerRepo.scannerState(ScannerState.HoldScanError)
//                    }
//                    else -> {
//                        val boxOut = box.copy(deliveredAt = scanTime)
//                        result =
//                            CourierUnloadingScanBoxData.BoxAdded(parsedScan.boxId, boxOut.address)
//                        localRepo.unloadBox(boxOut)
//                        scannerRepo.scannerState(ScannerState.HoldScanComplete)
//                    }
//                }
//
//                localRepo.findOfficeById(officeId)
//                    .flatMapObservable {
//                        Observable.just(
//                            CourierUnloadingProcessData(result, it.deliveredBoxes, it.countBoxes)
//                        ).mergeWith(scannerRepo.holdStart())
//                    }
//                    .compose(rxSchedulerFactory.applyObservableSchedulers())
//            }
//    }


    var scanLoaderProgressSubject = MutableLiveData<CourierUnloadingProgressData>()
    override suspend fun getCurrentOffice(officeId: Int): LocalOfficeEntity {
        return withContext(Dispatchers.IO) {
            localRepo.findOfficeById(officeId)
        }
    }

    override suspend fun scanLoaderProgress(): CourierUnloadingProgressData {
        return scanLoaderProgressSubject.value!!
    }

    //    override suspend fun removeScannedBoxes(checkedBoxes: List<String>)  {
//        return Completable.complete()
//            .compose(rxSchedulerFactory.applyCompletableSchedulers())
//    }
    override suspend fun removeScannedBoxes(checkedBoxes: List<String>) {
        return withContext(Dispatchers.IO) {

        }
    }

    override suspend fun scannerAction(scannerAction: ScannerState) {
        scannerRepo.scannerState(scannerAction)
    }

    override suspend fun observeOrderData(): CourierOrderLocalDataEntity {
        return withContext(Dispatchers.IO) {
            localRepo.observeOrderData()
        }
    }

    override suspend fun completeOfficeUnload() {
        val boxes = localRepo.getOfflineBoxes()
        boxes.find { b -> b.deliveredAt != "" }
        return withContext(Dispatchers.IO) {
            remoteRepo.setIntransitTask(localRepo.getOrderId(), boxes)
            localRepo.setOnlineOffices()
        }
    }

    override suspend fun getRemainBoxes(officeId: Int): List<LocalBoxEntity> {
        return localRepo.getRemainBoxes(officeId)
    }

    override suspend fun getOrderId(): String {
        // FIXME: У одного курьера здесь происходит NullPointerException. Причина пока не понятна
        return localRepo.getOrder().orderId.toString()
    }

}