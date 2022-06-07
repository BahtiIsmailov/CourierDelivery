package ru.wb.go.ui.courierloading.domain

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.wb.go.db.CourierLocalRepository
import ru.wb.go.db.TaskTimerRepository
import ru.wb.go.db.entity.courierlocal.CourierOrderLocalDataEntity
import ru.wb.go.db.entity.courierlocal.LocalBoxEntity
import ru.wb.go.db.entity.courierlocal.LocalLoadingBoxEntity
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

    override suspend fun scannedBoxes(): List<LocalBoxEntity> {
        return withContext(Dispatchers.IO) {
            localRepo.readAllLoadingBoxesSync()
        }
    }

    private fun scanResult(
        scannerState: ScannerState,
        data: CourierLoadingScanBoxData,
        boxCount: Int
    ):  CourierLoadingProcessData  {
        scanRepo.scannerState(scannerState)
        scanRepo.holdStart()
        CourierLoadingProcessData(
            CourierLoadingScanBoxData.ScannerReady,
            boxCount
        )
        return CourierLoadingProcessData(data, boxCount)



    }
//    private fun scanResult(
//        scannerState: ScannerState,
//        data: CourierLoadingScanBoxData,
//        boxCount: Int
//    ): Observable<CourierLoadingProcessData> {
//        scanRepo.scannerState(scannerState)
//        return Observable.just(CourierLoadingProcessData(data, boxCount))
//            .mergeWith(
//                scanRepo.holdStart()
//                    .andThen(
//                        Observable.just(
//                            CourierLoadingProcessData(
//                                CourierLoadingScanBoxData.ScannerReady,
//                                boxCount
//                            )
//                        )
//                    )
//            )
//
//    }

    override suspend fun observeScanProcess():  CourierLoadingProcessData {
        withContext(Dispatchers.IO) {
            scanRepo.observeScannerAction()
        }
    }


//    override fun observeScanProcess(): Observable<CourierLoadingProcessData> {
//        return scanRepo.observeScannerAction()
//            .filter { it is ScannerAction.ScanResult }
//            .map { it as ScannerAction.ScanResult }
//            .map { scanRepo.parseScanBoxQr(it.value) }
//            .flatMap { parsedScan ->
//                val boxes = localRepo.getBoxes()
//                val scanTime = timeManager.getLocalTime()
//                if (!parsedScan.isOk) {
//                    scanResult(
//                        ScannerState.HoldScanUnknown,
//                        CourierLoadingScanBoxData.NotRecognizedQr,
//                        boxes.size
//                    )
//                } else {
//                    val offices = localRepo.getOffices()
//                    val office =
//                        offices.find { off -> off.officeId.toString() == parsedScan.officeId }
//                    var box = boxes.find { b -> b.boxId == parsedScan.boxId }
//                    if (office == null || (box != null && box.officeId.toString() != parsedScan.officeId)) {
//                        scanResult(
//                            ScannerState.HoldScanError,
//                            CourierLoadingScanBoxData.ForbiddenTakeBox(parsedScan.boxId),
//                            boxes.size
//                        )
//                    } else {
//                        var isNewBox = false
//                        if (box == null) {
//                            isNewBox = true
//                            box = LocalBoxEntity(
//                                boxId = parsedScan.boxId, officeId = office.officeId,
//                                address = office.address, loadingAt = scanTime, deliveredAt = ""
//                            )
//                        } else {
//                            box = box.copy(loadingAt = scanTime)
//                        }
//
//                        qrComplete(box, boxes.size, isNewBox, scanTime)
//
//                    }
//                }
//            }
//            .compose(rxSchedulerFactory.applyObservableSchedulers())
//    }

    private suspend fun qrComplete(
        box: LocalBoxEntity,
        countBox: Int,
        isNewBox: Boolean,
        scanTime: String
    ): CourierLoadingProcessData {
        return withContext(Dispatchers.IO) {
            when (countBox) {
                0 -> {
                    firstBoxLoaderProgress()
                    remoteRepo.setStartTask(localRepo.getOrderId(), box)
                    firstBoxLoaderComplete()
                    localRepo.loadBoxOnboard(box, true)
                    taskTimerRepository.stopTimer()
                    localRepo.setOrderOrderStart(scanTime)
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

    }

//    private fun qrComplete(
//        box: LocalBoxEntity,
//        countBox: Int,
//        isNewBox: Boolean,
//        scanTime: String
//    ): Observable<CourierLoadingProcessData> {
//        return when (countBox) {
//            0 ->
//                firstBoxLoaderProgress()
//                    .andThen(
//                        localRepo.getOrderId()
//                            .flatMapObservable {
//                                remoteRepo.setStartTask(it, box) // done
//                                    .doFinally { firstBoxLoaderComplete() }
//                                    .flatMapObservable {
//                                        localRepo.loadBoxOnboard(box, true)
//                                            .doOnComplete {
//                                                taskTimerRepository.stopTimer()
//                                                localRepo.setOrderOrderStart(scanTime)
//                                            }
//                                            .andThen(
//                                                scanResult(
//                                                    ScannerState.HoldScanComplete,
//                                                    CourierLoadingScanBoxData.FirstBoxAdded(
//                                                        box.boxId,
//                                                        box.address
//                                                    ),
//                                                    1
//                                                )
//                                            )
//                                    }
//                            })
//            else -> {
//                localRepo.loadBoxOnboard(box, isNewBox)
//                    .andThen(
//                        scanResult(
//                            ScannerState.HoldScanComplete,
//                            CourierLoadingScanBoxData.SecondaryBoxAdded(box.boxId, box.address),
//                            when (isNewBox) {
//                                true -> countBox + 1
//                                else -> countBox
//                            }
//                        )
//                    )
//            }
//        }
//            .compose(rxSchedulerFactory.applyObservableSchedulers())
//    }

    private fun firstBoxLoaderProgress() {
        scanLoaderProgressSubject.onNext(CourierLoadingProgressData.Progress)
    }

//    private fun firstBoxLoaderProgress() = Completable.fromAction {
//        scanLoaderProgressSubject.onNext(CourierLoadingProgressData.Progress)
//    }

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

    override suspend fun deleteTask() {
        taskTimerRepository.stopTimer()
        return withContext(Dispatchers.IO) {
            remoteRepo.deleteTask(localRepo.getOrderId())
            localRepo.deleteOrder()
        }
    }

//    override   fun deleteTask(): Completable {
//        taskTimerRepository.stopTimer()
//        return localRepo.getOrderId()
//            .flatMapCompletable { remoteRepo.deleteTask(it) }
//            .doOnComplete { localRepo.deleteOrder() }
//            .compose(rxSchedulerFactory.applyCompletableSchedulers())
//    }

    override suspend fun confirmLoadingBoxes(): CourierCompleteData {
        return withContext(Dispatchers.IO) {
            val response = localRepo.readAllLoadingBoxesSync()
            val taskCost = remoteRepo.setReadyTask(localRepo.getOrderId(), response).coast
            localRepo.setOrderAfterLoadStatus(taskCost)
            CourierCompleteData(taskCost, response.size)
        }
    }


//        override fun confirmLoadingBoxes(): Single<CourierCompleteData> {
//            return localRepo.readAllLoadingBoxesSync() // done
//                .flatMap { boxes ->
//                    localRepo.getOrderId()
//                        .flatMap { taskId ->
//                            remoteRepo.setReadyTask(taskId, boxes)
//                                .map { it.coast } // done
//                                .doOnSuccess {
//                                    localRepo.setOrderAfterLoadStatus(it) // done
//                                }
//                                .map { CourierCompleteData(it, boxes.size) }
//                        }
//                }
//                .compose(rxSchedulerFactory.applySingleSchedulers())
//        }

    override suspend fun getGate(): String {
        return withContext(Dispatchers.IO) {
            localRepo.getOrderGate()
        }
    }

    override fun loadingBoxBoxesGroupByOffice(): Single<LoadingBoxGoals> {
        return localRepo.loadingBoxBoxesGroupByOffice()
            .map {
                var pvzCount = 0
                var boxCount = 0
                val localLoadingBoxEntities = mutableListOf<LocalLoadingBoxEntity>()
                it.forEach { localLoadingBox ->
                    pvzCount++
                    boxCount += localLoadingBox.count
                    localLoadingBoxEntities.add(localLoadingBox)
                }
                LoadingBoxGoals(pvzCount, boxCount, localLoadingBoxEntities)
            }
            .compose(rxSchedulerFactory.applySingleSchedulers())
    }

}

data class LoadingBoxGoals(
    var pvzCount: Int,
    var boxCount: Int,
    var localLoadingBoxEntity: MutableList<LocalLoadingBoxEntity>
)