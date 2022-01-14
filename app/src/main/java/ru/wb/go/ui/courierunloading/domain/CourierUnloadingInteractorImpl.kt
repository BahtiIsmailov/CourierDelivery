package ru.wb.go.ui.courierunloading.domain

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import ru.wb.go.db.CourierLocalRepository
import ru.wb.go.db.entity.courierlocal.CourierOrderLocalDataEntity
import ru.wb.go.db.entity.courierlocal.LocalOfficeEntity
import ru.wb.go.network.api.app.AppRemoteRepository
import ru.wb.go.network.monitor.NetworkMonitorRepository
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.network.rx.RxSchedulerFactory
import ru.wb.go.ui.scanner.domain.ScannerRepository
import ru.wb.go.ui.scanner.domain.ScannerState
import ru.wb.go.utils.managers.TimeManager

class CourierUnloadingInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val networkMonitorRepository: NetworkMonitorRepository,
    private val remoteRepo: AppRemoteRepository,
    private val scannerRepo: ScannerRepository,
    private val timeManager: TimeManager,
    private val localRepo: CourierLocalRepository,
) : CourierUnloadingInteractor {

    companion object {
        const val EMPTY_ADDRESS = ""
    }

    private val scanLoaderProgressSubject = PublishSubject.create<CourierUnloadingProgressData>()

    override fun getCurrentOffice(officeId: Int): Single<LocalOfficeEntity> {
        return localRepo.findOfficeById(officeId)
            .compose(rxSchedulerFactory.applySingleSchedulers())
    }

    override fun observeNetworkConnected(): Observable<NetworkState> {
        return networkMonitorRepository.networkConnected()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    override fun observeScanProcess(officeId: Int): Observable<CourierUnloadingProcessData> {
        return scannerRepo.observeBarcodeScanned()
            .map {
                scannerRepo.parseScanBoxQr(it)
            }
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

    override fun completeOfficeUnload(): Completable {
        val boxes = localRepo.getOfflineBoxes()
        boxes.find { b -> b.deliveredAt != "" } ?: return Completable.complete()
        return localRepo.getOrderId()
            .flatMapCompletable {
                remoteRepo.setIntransitTask(it, boxes)
            }
            .doOnComplete {
                localRepo.setOnlineOffices()
            }
            .compose(rxSchedulerFactory.applyCompletableSchedulers())
    }
}