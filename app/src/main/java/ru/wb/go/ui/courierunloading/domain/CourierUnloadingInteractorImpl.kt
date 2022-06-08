package ru.wb.go.ui.courierunloading.domain

import io.reactivex.*
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.Dispatchers
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

    private val scanLoaderProgressSubject = PublishSubject.create<CourierUnloadingProgressData>()

    override fun getCurrentOffice(officeId: Int): Single<LocalOfficeEntity> {
        return localRepo.findOfficeById(officeId)
            .compose(rxSchedulerFactory.applySingleSchedulers())
    }

    override fun observeScanProcess(officeId: Int): Observable<CourierUnloadingProcessData> {
        return scannerRepo.observeScannerAction()
            .filter { it is ScannerAction.ScanResult }
            .map { it as ScannerAction.ScanResult }
            .map { scannerRepo.parseScanBoxQr(it.value) }
            .flatMap { parsedScan ->
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

                localRepo.findOfficeById(officeId)
                    .flatMapObservable {
                        Observable.just(
                            CourierUnloadingProcessData(result, it.deliveredBoxes, it.countBoxes)
                        ).mergeWith(scannerRepo.holdStart())
                    }
                    .compose(rxSchedulerFactory.applyObservableSchedulers())
            }
    }

    override fun scanLoaderProgress(): Observable<CourierUnloadingProgressData> {
        return scanLoaderProgressSubject
    }

    override fun removeScannedBoxes(checkedBoxes: List<String>): Completable {
        return Completable.complete()
            .compose(rxSchedulerFactory.applyCompletableSchedulers())
    }

    override fun scannerAction(scannerAction: ScannerState) {
        scannerRepo.scannerState(scannerAction)
    }

    override fun observeOrderData(): Flowable<CourierOrderLocalDataEntity> {
        return localRepo.observeOrderData()
            .compose(rxSchedulerFactory.applyFlowableSchedulers())
    }

    override suspend fun completeOfficeUnload() {
        val boxes = localRepo.getOfflineBoxes()
        boxes.find { b -> b.deliveredAt != "" }
        return withContext(Dispatchers.IO) {
            remoteRepo.setIntransitTask(localRepo.getOrderId(), boxes)
            localRepo.setOnlineOffices()
        }
    }

    override fun getRemainBoxes(officeId: Int): Maybe<List<LocalBoxEntity>> {
        return localRepo.getRemainBoxes(officeId)
    }

    override fun getOrderId(): String {
        // FIXME: У одного курьера здесь происходит NullPointerException. Причина пока не понятна
        return localRepo.getOrder().orderId.toString()
    }

}