package ru.wb.go.ui.courierloading.domain

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import ru.wb.go.app.PREFIX_QR_CODE
import ru.wb.go.db.CourierLocalRepository
import ru.wb.go.db.TaskTimerRepository
import ru.wb.go.db.entity.TaskStatus
import ru.wb.go.db.entity.courierboxes.CourierBoxEntity
import ru.wb.go.db.entity.courierlocal.CourierLoadingInfoEntity
import ru.wb.go.db.entity.courierlocal.CourierOrderDstOfficeLocalEntity
import ru.wb.go.db.entity.courierlocal.CourierOrderLocalDataEntity
import ru.wb.go.network.api.app.AppRemoteRepository
import ru.wb.go.network.api.app.entity.CourierTaskStartEntity
import ru.wb.go.network.api.app.entity.CourierTaskStatusesIntransitEntity
import ru.wb.go.network.monitor.NetworkMonitorRepository
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.network.rx.RxSchedulerFactory
import ru.wb.go.network.token.UserManager
import ru.wb.go.ui.scanner.domain.ScannerRepository
import ru.wb.go.ui.scanner.domain.ScannerState
import ru.wb.go.utils.LogUtils
import ru.wb.go.utils.managers.TimeManager

class CourierLoadingInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val networkMonitorRepository: NetworkMonitorRepository,
    private val appRemoteRepository: AppRemoteRepository,
    private val scannerRepository: ScannerRepository,
    private val timeManager: TimeManager,
    private val courierLocalRepository: CourierLocalRepository,
    private val taskTimerRepository: TaskTimerRepository,
    private val userManager: UserManager
) : CourierLoadingInteractor {

    private val scanLoaderProgressSubject = PublishSubject.create<CourierLoadingProgressData>()

    override fun observeNetworkConnected(): Observable<NetworkState> {
        return networkMonitorRepository.networkConnected()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    override fun scannedBoxes(): Single<List<CourierBoxEntity>> {
        return courierLocalRepository.readAllLoadingBoxes()
            .compose(rxSchedulerFactory.applySingleSchedulers())
    }

    private fun observeCourierBoxes(): Single<List<CourierBoxEntity>> {
        return courierLocalRepository.observeLoadingBoxes().firstOrError()
    }

    override fun observeScanProcess(): Observable<CourierLoadingProcessData> {
        return observeBoxDefinitionResult().flatMap { scanResult ->
            observeCourierBoxes().flatMapObservable { boxes ->

                LogUtils { logDebugApp("flatMap processData " + scanResult + " / " + boxes.size) }

                val orderDstOffice = scanResult.orderDstOffice
                val count = boxes.size
                val qrcode = scanResult.parseQrCode.code

                if (orderDstOffice == null) {
                    LogUtils { logDebugApp("orderDstOffice == null UnknownBox") }
                    justProcessData(CourierLoadingScanBoxData.UnknownBox(qrcode), count)
                } else {

                    val address = orderDstOffice.fullAddress
                    val dstOfficeId = orderDstOffice.id
                    val loadingAt = scanResult.loadingAt

                    val courierBoxEntity = CourierBoxEntity(
                        id = qrcode,
                        address = address,
                        dstOfficeId = dstOfficeId,
                        loadingAt = loadingAt,
                        deliveredAt = ""
                    )

                    if (boxes.isEmpty()) {
                        LogUtils { logDebugApp("processData.second isEmpty() " + boxes) }
                        loaderProgress()
                        val courierTaskStartEntity =
                            CourierTaskStartEntity(
                                id = qrcode,
                                dstOfficeID = dstOfficeId,
                                loadingAt = loadingAt
                            )
                        val boxFirstAdded =
                            justProcessData(
                                CourierLoadingScanBoxData.BoxFirstAdded(
                                    qrcode,
                                    address
                                ), 1
                            )

                        taskStart(scanResult.taskId, courierTaskStartEntity)
                            .andThen(saveBoxLocal(courierBoxEntity))
                            .andThen(boxFirstAdded)
                            .doOnComplete { firstBoxAddedComplete() }
                            .compose(rxSchedulerFactory.applyObservableSchedulers())
                    } else {
                        LogUtils { logDebugApp("processData.second " + boxes.size) }
                        saveBoxLocal(courierBoxEntity).andThen(
                            observeCourierBoxes().map { it.size }
                                .flatMapObservable { count1 ->
                                    justProcessData(
                                        CourierLoadingScanBoxData.BoxAdded(
                                            qrcode,
                                            address
                                        ), count1
                                    )
                                })
                    }
                }
            }
        }
            .doOnError { loaderComplete() }
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    private fun firstBoxAddedComplete() {
        taskTimerRepository.stopTimer()
        userManager.saveStatusTask(TaskStatus.STARTED.status)
        loaderComplete()
    }

    private fun justProcessData(
        scanBoxData: CourierLoadingScanBoxData,
        count: Int
    ): Observable<CourierLoadingProcessData> {
        return Observable.just(CourierLoadingProcessData(scanBoxData, count))
    }

    private fun taskStart(
        taskId: String,
        courierTaskStartEntity: CourierTaskStartEntity
    ): Completable {
        // TODO: 24.09.2021 выключить для тестирования
        //return Completable.timer(3, TimeUnit.SECONDS)
        return appRemoteRepository.taskStart(taskId, courierTaskStartEntity)
    }

    private fun loaderProgress() {
        scanLoaderProgressSubject.onNext(CourierLoadingProgressData.Progress)
        scannerRepository.scannerState(ScannerState.LoaderProgress)
    }

    private fun observeBoxDefinitionResult(): Observable<CourierBoxDefinitionResult> {
        return scannerRepository.observeBarcodeScanned()
            .filter { it.startsWith(PREFIX_QR_CODE) }
            .map { parseQrCode(it) }
            .flatMapSingle { boxDefinitionResult(it) }
    }

    private fun boxDefinitionResult(parseQrCode: ParseQrCode): Single<CourierBoxDefinitionResult> {
        return Single.zip(orderDstOffices(), updatedAt(), taskId(),
            { orderDstOffices, updatedAt, taskId ->
                CourierBoxDefinitionResult(
                    findOfficeById(orderDstOffices, parseQrCode.dstOfficeId),
                    parseQrCode,
                    updatedAt,
                    taskId
                )
            }
        )
            .doOnError { LogUtils { logDebugApp(it.toString()) } }
    }

    private fun orderDstOffices(): Single<List<CourierOrderDstOfficeLocalEntity>> {
        return courierLocalRepository.orderData().map { it.dstOffices }
    }

    private fun findOfficeById(
        orderDstOffices: List<CourierOrderDstOfficeLocalEntity>,
        dstOfficeId: String
    ): CourierOrderDstOfficeLocalEntity? {
        return orderDstOffices.find { it.id.toString() == dstOfficeId }
    }

    private fun saveBoxLocal(courierBoxEntity: CourierBoxEntity): Completable {
        return courierLocalRepository.saveLoadingBox(courierBoxEntity)
    }

    private fun parseQrCode(qrCode: String): ParseQrCode {
        val parseParams = getSplitInfo(getInfo(qrCode))
        return ParseQrCode(parseParams[0], parseParams[1])
    }

    private fun getSplitInfo(input: String): List<String> {
        return input.split(":")
    }

    private fun getInfo(input: String): String {
        return input.takeLast(input.length - PREFIX_QR_CODE.length)
    }

    private fun loaderComplete() {
        scanLoaderProgressSubject.onNext(CourierLoadingProgressData.Complete)
        scannerRepository.scannerState(ScannerState.LoaderComplete)
    }

    override fun scanLoaderProgress(): Observable<CourierLoadingProgressData> {
        return scanLoaderProgressSubject
    }

    override fun removeScannedBoxes(checkedBoxes: List<String>): Completable {
        return courierLocalRepository.deleteLoadingBoxesByQrCode(checkedBoxes)
            .compose(rxSchedulerFactory.applyCompletableSchedulers())
    }

    private fun updatedAt() = Single.just(timeManager.getLocalTime())

    override fun scannerAction(scannerAction: ScannerState) {
        scannerRepository.scannerState(scannerAction)
    }

    override fun observeOrderData(): Flowable<CourierOrderLocalDataEntity> {
        return courierLocalRepository.observeOrderData()
            .compose(rxSchedulerFactory.applyFlowableSchedulers())
    }

    override fun deleteTask(): Completable {
        return taskId().flatMapCompletable { appRemoteRepository.deleteTask(it) }
            .compose(rxSchedulerFactory.applyCompletableSchedulers())
    }

    override fun confirmLoadingBoxes(): Single<CourierCompleteData> {
        return courierLocalRepository.readAllLoadingBoxes()
            .flatMap { convertToCourierTaskStatusesIntransitEntity(it) }
            .flatMap { intransitBoxes ->
                taskId().flatMap { taskId ->
                    // TODO: 24.09.2021 включить для тестирования
                    //Completable.timer(3, TimeUnit.SECONDS).andThen(Completable.error(Throwable()))
                    //Completable.timer(3, TimeUnit.SECONDS).andThen(Single.just(CourierCompleteData(1200, 10)))
                    appRemoteRepository.taskStatusesReady(taskId, intransitBoxes)
                        .map { it.coast }
                        .doOnSuccess {
                            userManager.saveStatusTask(TaskStatus.INTRANSIT.status)
                            userManager.saveCostTask(it)
                        }
                        .map { CourierCompleteData(it, intransitBoxes.size) }
                        .compose(rxSchedulerFactory.applySingleSchedulers())
                }
            }
            .compose(rxSchedulerFactory.applySingleSchedulers())
    }

    override fun info(): Single<CourierLoadingInfoEntity> {
        return courierLocalRepository.courierLoadingInfoEntity()
            .compose(rxSchedulerFactory.applySingleSchedulers())
    }

    private fun convertToCourierTaskStatusesIntransitEntity(item: List<CourierBoxEntity>) =
        Observable.fromIterable(item).map {
            with(it) {
                CourierTaskStatusesIntransitEntity(
                    id = id,
                    dstOfficeID = dstOfficeId,
                    loadingAt = loadingAt,
                    deliveredAt = null
                )
            }
        }.toList()

    private fun taskId() =
        courierLocalRepository.observeOrderData()
            .map { it.courierOrderLocalEntity.id.toString() }
            .first("")

}

data class ParseQrCode(val code: String, val dstOfficeId: String)