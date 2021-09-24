package ru.wb.perevozka.ui.courierloading.domain

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import ru.wb.perevozka.app.PREFIX_QR_CODE
import ru.wb.perevozka.db.AppLocalRepository
import ru.wb.perevozka.db.CourierLocalRepository
import ru.wb.perevozka.db.TaskTimerRepository
import ru.wb.perevozka.db.entity.courierboxes.CourierBoxEntity
import ru.wb.perevozka.db.entity.courierlocal.CourierOrderDstOfficeLocalEntity
import ru.wb.perevozka.db.entity.courierlocal.CourierOrderLocalDataEntity
import ru.wb.perevozka.network.api.app.AppRemoteRepository
import ru.wb.perevozka.network.api.app.FlightStatus
import ru.wb.perevozka.network.api.app.entity.CourierTaskStartEntity
import ru.wb.perevozka.network.api.app.entity.CourierTaskStatusesIntransitEntity
import ru.wb.perevozka.network.monitor.NetworkMonitorRepository
import ru.wb.perevozka.network.monitor.NetworkState
import ru.wb.perevozka.network.rx.RxSchedulerFactory
import ru.wb.perevozka.ui.scanner.domain.ScannerRepository
import ru.wb.perevozka.ui.scanner.domain.ScannerState
import ru.wb.perevozka.utils.LogUtils
import ru.wb.perevozka.utils.managers.ScreenManager
import ru.wb.perevozka.utils.managers.TimeManager
import java.util.concurrent.TimeUnit

class CourierLoadingInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val networkMonitorRepository: NetworkMonitorRepository,
    private val appRemoteRepository: AppRemoteRepository,
    private val appLocalRepository: AppLocalRepository,
    private val scannerRepository: ScannerRepository,
    private val timeManager: TimeManager,
    private val screenManager: ScreenManager,
    private val courierLocalRepository: CourierLocalRepository,
    private val taskTimerRepository: TaskTimerRepository
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

    private fun observeCourierBoxesCount(): Observable<List<CourierBoxEntity>> {
        return courierLocalRepository.observeLoadingBoxes().toObservable()
            .doOnNext { LogUtils { logDebugApp("observeCourierBoxesCount " + it.size) } }
    }

    override fun observeScanProcess(): Observable<CourierLoadingProcessData> {
        return Observable.combineLatest(observeBoxDefinitionResult(),
            observeCourierBoxesCount().distinct(),
            { boxDefinitionResult, boxes ->
                LogUtils { logDebugApp("boxDefinitionResult, boxes " + boxDefinitionResult.toString() + " " + boxes) }
                Pair(boxDefinitionResult, boxes)
            })
            .flatMap { processData ->
                LogUtils { logDebugApp("flatMap processData " + processData) }

                val orderDstOffice = processData.first.orderDstOffice
                val count = processData.second.size
                val qrcode = processData.first.parseQrCode.code

                if (orderDstOffice == null) {
                    LogUtils { logDebugApp("orderDstOffice == null") }
                    justProcessData(CourierLoadingScanBoxData.UnknownBox(qrcode), count)
                } else {

                    val address = orderDstOffice.fullAddress
                    val dstOfficeId = orderDstOffice.id
                    val loadingAt = processData.first.loadingAt

                    val courierBoxEntity = CourierBoxEntity(
                        id = qrcode,
                        address = address,
                        dstOfficeId = dstOfficeId,
                        loadingAt = loadingAt,
                        deliveredAt = ""
                    )

                    if (processData.second.isEmpty()) {
                        LogUtils { logDebugApp("processData.second isEmpty() " + processData.second) }
                        loaderProgress()
                        val courierTaskStartEntity =
                            CourierTaskStartEntity(
                                id = qrcode,
                                dstOfficeID = dstOfficeId,
                                loadingAt = loadingAt
                            )

                        taskStart(processData.first.taskId, courierTaskStartEntity)
                            .andThen(saveBoxLocal(courierBoxEntity))
                            .andThen(
                                justProcessData(
                                    CourierLoadingScanBoxData.BoxAdded(
                                        qrcode,
                                        address
                                    ), count
                                )
                            )
                            .doOnComplete {
                                taskTimerRepository.stopTimer()
                                loaderComplete()
                            }
                            .compose(rxSchedulerFactory.applyObservableSchedulers())
                    } else {
                        LogUtils { logDebugApp("processData.second " + processData.second) }
                        saveBoxLocal(courierBoxEntity).andThen(
                            justProcessData(
                                CourierLoadingScanBoxData.BoxAdded(qrcode, address),
                                count
                            )
                        )
                    }

                }
            }
            .doOnError { loaderComplete() }
            .compose(rxSchedulerFactory.applyObservableSchedulers())
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

    override fun switchScreen(): Completable {
        return screenManager.saveState(FlightStatus.DCLOADING)
    }

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

    override fun confirmLoadingBoxes(): Completable {
        return courierLocalRepository.readAllLoadingBoxes()
            .flatMap { convertToCourierTaskStatusesIntransitEntity(it) }
            .flatMapCompletable { statusesIntransit ->
                taskId().flatMapCompletable { taskId ->
                    // TODO: 24.09.2021 выключить для тестирования
                    //Completable.timer(3, TimeUnit.SECONDS).andThen(Completable.error(Throwable()))
                    appRemoteRepository.taskStatusesIntransit(taskId, statusesIntransit)
                        .compose(rxSchedulerFactory.applyCompletableSchedulers())
                }
            }
            .compose(rxSchedulerFactory.applyCompletableSchedulers())
    }

    private fun convertToCourierTaskStatusesIntransitEntity(item: List<CourierBoxEntity>) =
        Observable.fromIterable(item).map {
            with(it) {
                CourierTaskStatusesIntransitEntity(
                    id = id,
                    dstOfficeID = dstOfficeId,
                    loadingAt = loadingAt,
                    deliveredAt = deliveredAt
                )
            }
        }.toList()

    private fun taskId() =
        courierLocalRepository.observeOrderData()
            .map { it.courierOrderLocalEntity.id.toString() }
            .first("")

}

data class ParseQrCode(val code: String, val dstOfficeId: String)