package ru.wb.go.ui.courierloading.domain

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import ru.wb.go.db.CourierLocalRepository
import ru.wb.go.db.TaskTimerRepository
import ru.wb.go.db.entity.courierlocal.CourierOrderLocalDataEntity
import ru.wb.go.db.entity.courierlocal.LocalBoxEntity
import ru.wb.go.network.api.app.AppRemoteRepository
import ru.wb.go.network.monitor.NetworkMonitorRepository
import ru.wb.go.network.rx.RxSchedulerFactory
import ru.wb.go.ui.BaseServiceInteractorImpl
import ru.wb.go.ui.scanner.domain.ScannerAction
import ru.wb.go.ui.scanner.domain.ScannerRepository
import ru.wb.go.ui.scanner.domain.ScannerState
import ru.wb.go.utils.managers.DeviceManager
import ru.wb.go.utils.managers.TimeManager

class CourierLoadingInteractorImpl(
    rxSchedulerFactory: RxSchedulerFactory,
    networkMonitorRepository: NetworkMonitorRepository,
    deviceManager: DeviceManager,
    private val remoteRepo: AppRemoteRepository,
    private val scanRepo: ScannerRepository,
    private val timeManager: TimeManager,
    private val localRepo: CourierLocalRepository,
    private val taskTimerRepository: TaskTimerRepository,
) : BaseServiceInteractorImpl(rxSchedulerFactory, networkMonitorRepository, deviceManager),
    CourierLoadingInteractor {

    private val scanLoaderProgressSubject = PublishSubject.create<CourierLoadingProgressData>()

    companion object {
        const val DELAY_HOLD_SCANNER = 1500L
    }

    override fun scannedBoxes(): Single<List<LocalBoxEntity>> {
        return localRepo.readAllLoadingBoxesSync()
            .compose(rxSchedulerFactory.applySingleSchedulers())
    }

    private fun scanResult(
        scannerState: ScannerState,
        data: CourierLoadingScanBoxData,
        boxCount: Int
    ): Observable<CourierLoadingProcessData> {
        scanRepo.scannerState(scannerState)
        return Observable.just(CourierLoadingProcessData(data, boxCount))
            .mergeWith(
                scanRepo.holdStart()
                    .andThen(
                        Observable.just(
                            CourierLoadingProcessData(
                                CourierLoadingScanBoxData.ScannerReady,
                                boxCount
                            )
                        )
                    )
            )

    }

    override fun observeScanProcess(): Observable<CourierLoadingProcessData> {
        return scanRepo.observeScannerAction()
            .filter { it is ScannerAction.ScanResult }
            .map { it as ScannerAction.ScanResult }
            .map { scanRepo.parseScanBoxQr(it.value) }
            .flatMap { parsedScan ->
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
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    private fun qrComplete(
        box: LocalBoxEntity,
        countBox: Int,
        isNewBox: Boolean,
        scanTime: String
    ): Observable<CourierLoadingProcessData> {
        return when (countBox) {
            0 ->
                firstBoxLoaderProgress()
                    .andThen(
                        localRepo.getOrderId()
                            .flatMapObservable {
                                remoteRepo.setStartTask(it, box)
                                    .doFinally { firstBoxLoaderComplete() }
                                    .flatMapObservable {
                                        localRepo.loadBoxOnboard(box, true)
                                            .doOnComplete {
                                                taskTimerRepository.stopTimer()
                                                localRepo.setOrderOrderStart(scanTime)
                                            }
                                            .andThen(
                                                scanResult(
                                                    ScannerState.HoldScanComplete,
                                                    CourierLoadingScanBoxData.FirstBoxAdded(
                                                        box.boxId,
                                                        box.address
                                                    ),
                                                    1
                                                )
                                            )
                                    }
                            })
            else -> {
                localRepo.loadBoxOnboard(box, isNewBox)
                    .andThen(
                        scanResult(
                            ScannerState.HoldScanComplete,
                            CourierLoadingScanBoxData.SecondaryBoxAdded(box.boxId, box.address),
                            when (isNewBox) {
                                true -> countBox + 1
                                else -> countBox
                            }
                        )
                    )
            }
        }
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    private fun firstBoxLoaderProgress() = Completable.fromAction {
        scanLoaderProgressSubject.onNext(CourierLoadingProgressData.Progress)
    }

    private fun firstBoxLoaderComplete() {
        scanLoaderProgressSubject.onNext(CourierLoadingProgressData.Complete)
    }

    override fun scanLoaderProgress(): Observable<CourierLoadingProgressData> {
        return scanLoaderProgressSubject
    }

    override fun scannerAction(scannerAction: ScannerState) {
        scanRepo.scannerState(scannerAction)
    }

    override fun observeOrderData(): Flowable<CourierOrderLocalDataEntity> {
        return localRepo.observeOrderData()
            .compose(rxSchedulerFactory.applyFlowableSchedulers())
    }

    override fun deleteTask(): Completable {
        taskTimerRepository.stopTimer()
        return localRepo.getOrderId()
            .flatMapCompletable { remoteRepo.deleteTask(it) }
            .doOnComplete { localRepo.deleteOrder() }
            .compose(rxSchedulerFactory.applyCompletableSchedulers())
    }

    override fun confirmLoadingBoxes(): Single<CourierCompleteData> {
        return localRepo.readAllLoadingBoxesSync()
            .flatMap { boxes ->
                localRepo.getOrderId()
                    .flatMap { taskId ->
                        remoteRepo.setReadyTask(taskId, boxes)
                            .map { it.coast }
                            .doOnSuccess {
                                localRepo.setOrderAfterLoadStatus(it)
                            }
                            .map { CourierCompleteData(it, boxes.size) }
                    }
            }
            .compose(rxSchedulerFactory.applySingleSchedulers())
    }

    override fun getGate(): Single<String> {
        return localRepo.getOrderGate()
            .compose(rxSchedulerFactory.applySingleSchedulers())
    }

}