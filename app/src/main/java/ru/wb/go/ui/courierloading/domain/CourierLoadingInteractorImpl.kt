package ru.wb.go.ui.courierloading.domain

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import ru.wb.go.app.PREFIX_QR_CODE
import ru.wb.go.app.PREFIX_QR_CODE_SPLITTER
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
import java.util.concurrent.TimeUnit

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

    companion object {
        const val DELAY_HOLD_SCANNER = 2500L
    }

    override fun observeNetworkConnected(): Observable<NetworkState> {
        return networkMonitorRepository.networkConnected()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    override fun scannedBoxes(): Single<List<CourierBoxEntity>> {
        return courierLocalRepository.readAllLoadingBoxesSync()
            .compose(rxSchedulerFactory.applySingleSchedulers())
    }

    private fun observeCourierBoxes(): Single<List<CourierBoxEntity>> {
        return courierLocalRepository.observeLoadingBoxes().firstOrError()
    }

    override fun observeScanProcess(): Observable<CourierLoadingProcessData> {
        return observeBoxDefinitionResult().flatMap { scanResult ->
            observeCourierBoxes().flatMapObservable { boxes ->
                val orderDstOffice = scanResult.orderDstOffice
                val count = boxes.size
                val qrcode = scanResult.parseQrCode.code
                when {
                    qrcode.isEmpty() -> {
                        scannerRepository.scannerState(ScannerState.HoldScanUnknown)
                        justProcessData(CourierLoadingScanBoxData.NotRecognizedQr(qrcode), count)
                            .mergeWith(holdDelay(count))
                    }
                    orderDstOffice == null -> {
                        scannerRepository.scannerState(ScannerState.HoldScanError)
                        justProcessData(CourierLoadingScanBoxData.ForbiddenTakeBox(qrcode), count)
                            .mergeWith(holdDelay(count))
                    }
                    else -> qrComplete(orderDstOffice, scanResult, qrcode, count)
                }
            }
        }
            .doOnError { firstBoxLoaderComplete() }
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    private fun holdDelay(count: Int) = Observable.timer(DELAY_HOLD_SCANNER, TimeUnit.MILLISECONDS)
        .doOnNext { scannerRepository.scannerState(ScannerState.Start) }
        .flatMap { justProcessData(CourierLoadingScanBoxData.ScannerReady, count) }

    private fun qrComplete(
        orderDstOffice: CourierOrderDstOfficeLocalEntity,
        scanResult: CourierBoxDefinitionResult,
        qrcode: String,
        countBox: Int
    ): Observable<CourierLoadingProcessData> {

        val address = orderDstOffice.fullAddress
        val dstOfficeId = orderDstOffice.id
        val loadingAt = scanResult.loadingAt
        val taskId = scanResult.taskId

        val courierBoxEntity = CourierBoxEntity(
            id = qrcode,
            address = address,
            dstOfficeId = dstOfficeId,
            loadingAt = loadingAt,
            deliveredAt = ""
        )
        return if (countBox == 0) {
            firstBoxAdded(
                qrcode,
                address,
                dstOfficeId,
                loadingAt,
                taskId,
                courierBoxEntity
            ).mergeWith(justProcessData(CourierLoadingScanBoxData.ScannerReady, 1))
        } else {
            scannerRepository.scannerState(ScannerState.HoldScanComplete)
            secondaryBoxAdded(courierBoxEntity, qrcode, address).mergeWith(holdDelay(countBox))
        }
    }

    private fun secondaryBoxAdded(
        courierBoxEntity: CourierBoxEntity,
        qrcode: String,
        address: String
    ) = saveBoxLocal(courierBoxEntity).andThen(
        observeCourierBoxes().map { it.size }
            .flatMapObservable { count1 ->
                justProcessData(
                    CourierLoadingScanBoxData.SecondaryBoxAdded(qrcode, address), count1
                )
            })

    private fun firstBoxAdded(
        qrcode: String,
        address: String,
        dstOfficeId: Int,
        loadingAt: String,
        taskId: String,
        courierBoxEntity: CourierBoxEntity
    ): Observable<CourierLoadingProcessData> {
        val courierTaskStartEntity = CourierTaskStartEntity(qrcode, dstOfficeId, loadingAt)
        val firstBoxAdded = firstBoxLoaderComplete()
            .andThen(justProcessData(CourierLoadingScanBoxData.FirstBoxAdded(qrcode, address), 1))
        return firstBoxLoaderProgress()
            .andThen(taskStart(taskId, courierTaskStartEntity))
            .andThen(saveBoxLocal(courierBoxEntity))
            .andThen(firstBoxAdded)
            .doOnComplete { firstBoxAddedComplete() }
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    private fun firstBoxAddedComplete() {
        taskTimerRepository.stopTimer()
        userManager.saveStatusTask(TaskStatus.STARTED.status)
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

    private fun firstBoxLoaderProgress() = Completable.fromAction {
        scanLoaderProgressSubject.onNext(CourierLoadingProgressData.Progress)
        scannerRepository.scannerState(ScannerState.LoaderProgress)
    }

    private fun firstBoxLoaderComplete() = Completable.fromAction {
        scanLoaderProgressSubject.onNext(CourierLoadingProgressData.Complete)
        scannerRepository.scannerState(ScannerState.LoaderComplete)
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
        return courierLocalRepository.orderDataSync().map { it.dstOffices }
    }

    private fun findOfficeById(
        orderDstOffices: List<CourierOrderDstOfficeLocalEntity>,
        dstOfficeId: String
    ): CourierOrderDstOfficeLocalEntity? {
        return orderDstOffices.find { it.id.toString() == dstOfficeId }
    }

    private fun saveBoxLocal(courierBoxEntity: CourierBoxEntity): Completable {
        //для тестирования приемки 250 коробок
//        val id = courierBoxEntity.id.toInt()
//        val address = courierBoxEntity.address
//        val dstOfficeId = courierBoxEntity.dstOfficeId
//        val loadingAt = courierBoxEntity.loadingAt
//        val deliveredAt = courierBoxEntity.deliveredAt
//        val boxes = mutableListOf<CourierBoxEntity>()
//        for (i in 1..250) {
//            val box = CourierBoxEntity(
//                id = (id + i).toString(),
//                address = address,
//                dstOfficeId = dstOfficeId,
//                loadingAt = loadingAt,
//                deliveredAt = deliveredAt
//            )
//            boxes.add(box)
//        }
//        return courierLocalRepository.saveLoadingBoxes(boxes)

        return courierLocalRepository.saveLoadingBox(courierBoxEntity)
    }

    private fun parseQrCode(qrCode: String): ParseQrCode {
        return if (qrCode.startsWith(PREFIX_QR_CODE)) {
            val parseParams = getSplitInfo(getInfo(qrCode))
            ParseQrCode(parseParams[0], parseParams[1])
        } else {
            ParseQrCode("", "")
        }
    }

    private fun getSplitInfo(input: String): List<String> {
        return input.split(PREFIX_QR_CODE_SPLITTER)
    }

    private fun getInfo(input: String): String {
        return input.takeLast(input.length - PREFIX_QR_CODE.length)
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
        return courierLocalRepository.readAllLoadingBoxesSync()
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