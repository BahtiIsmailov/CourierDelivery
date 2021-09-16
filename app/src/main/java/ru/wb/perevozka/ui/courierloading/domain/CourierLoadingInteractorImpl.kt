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
import ru.wb.perevozka.ui.scanner.domain.ScannerAction
import ru.wb.perevozka.ui.scanner.domain.ScannerRepository
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
        return courierLocalRepository.observeLoadingBoxes()
            .toObservable()
            .doOnNext { LogUtils { logDebugApp("observeCourierBoxesCount " + it.size) } }
//            .map { it.size }
        //           .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    override fun observeScanProcess(): Observable<CourierLoadingProcessData> {
        return Observable.combineLatest(observeCourierScan(), observeCourierBoxesCount(),
            { scan, boxes -> CourierLoadingProcessData(scan, boxes, boxes.size) })
            .flatMap { processData ->
                LogUtils { logDebugApp("flatMap processData " + processData) }
                if (processData.scanBoxData is CourierLoadingScanBoxData.BoxAdded && processData.count == 1) {
                    taskTimerRepository.stopTimer()
                    val courierTaskStartEntity = with(processData.boxes.last()) {
                        CourierTaskStartEntity(
                            id,
                            dstOfficeId,
                            loadingAt
                        )
                    }
                    observeOrderData().toObservable()
                        .map { courierOrderLocalEntity -> courierOrderLocalEntity.courierOrderLocalEntity.id.toString() }
                        .flatMap { taskId ->
                            loaderProgress()
//                            Completable.timer(3, TimeUnit.SECONDS)

                            appRemoteRepository.taskStart(taskId, courierTaskStartEntity)
                                .doOnComplete { loaderComplete() }
                                .andThen(Observable.just(processData))
                                .compose(rxSchedulerFactory.applyObservableSchedulers())
                        }


                } else {
                    Observable.just(processData)
                }
            }
            .doOnError { loaderComplete() }
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    private fun loaderProgress() {
        scanLoaderProgressSubject.onNext(CourierLoadingProgressData.Progress)
        scannerRepository.scannerAction(ScannerAction.LoaderProgress)
    }

    private fun observeCourierScan(): Observable<CourierLoadingScanBoxData> {
        return scannerRepository.observeBarcodeScanned()
            .map { parseQrCode(it) }
            .flatMapSingle { boxDefinitionResult(it) }
            .flatMap { observableStatus(it) }
//            .filter{result -> result.courierOrderLocalDataEntity.dstOffices.find { it.id.toString() == result.parseQrCode.dstOfficeId } == null}
//            .flatMap { Observable.just(CourierLoadingScanBoxData.UnknownBox) }
//            .switchIfEmpty { boxAdded(result, dstOffice) }
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    private fun boxDefinitionResult(parseQrCode: ParseQrCode): Single<CourierLoadingDefinitionResult> {
        return Single.zip(
            courierLoadingScanBoxData(),
            updatedAt(),
            { courierOrderLocalDataEntity, updatedAt ->
                CourierLoadingDefinitionResult(
                    courierOrderLocalDataEntity,
                    parseQrCode,
                    updatedAt
                )
            }
        ).doOnError { LogUtils { logDebugApp(it.toString()) } }
            .compose(rxSchedulerFactory.applySingleSchedulers())
    }

    private fun observableStatus(result: CourierLoadingDefinitionResult): Observable<out CourierLoadingScanBoxData> {
        val dstOffice = result.courierOrderLocalDataEntity.dstOffices
            .find { it.id.toString() == result.parseQrCode.dstOfficeId }
        return if (dstOffice == null) {
            Observable.just(CourierLoadingScanBoxData.UnknownBox)
        } else {
            boxAdded(result, dstOffice)
        }
    }

    private fun boxAdded(
        result: CourierLoadingDefinitionResult,
        dstOffice: CourierOrderDstOfficeLocalEntity
    ): Observable<CourierLoadingScanBoxData> {
        val qrcode = result.parseQrCode.code
        val fullAddress = dstOffice.fullAddress
        val courierBoxEntity = CourierBoxEntity(
            id = qrcode,
            address = fullAddress,
            dstOfficeId = dstOffice.id,
            loadingAt = result.timeScan,
            deliveredAt = ""
        )
        return courierLocalRepository.saveLoadingBox(courierBoxEntity)
            .andThen(Observable.just(CourierLoadingScanBoxData.BoxAdded(qrcode, fullAddress)))
    }

    private fun parseQrCode(qrCode: String): ParseQrCode {
        val parseParams = getSplitInfo(getInfo(qrCode))
        return ParseQrCode(parseParams[0], parseParams[1])
    }

    private fun getInfo(input: String): String {
        return input.takeLast(input.length - PREFIX_QR_CODE.length)
    }

    private fun getSplitInfo(input: String): List<String> {
        return input.split(":")
    }

    private fun loaderComplete() {
        scanLoaderProgressSubject.onNext(CourierLoadingProgressData.Complete)
        scannerRepository.scannerAction(ScannerAction.LoaderComplete)
    }

    override fun scanLoaderProgress(): Observable<CourierLoadingProgressData> {
        return scanLoaderProgressSubject
    }

    override fun removeScannedBoxes(checkedBoxes: List<String>): Completable {
        return courierLocalRepository.deleteLoadingBoxesByQrCode(checkedBoxes)
            .compose(rxSchedulerFactory.applyCompletableSchedulers())
    }

    private fun courierLoadingScanBoxData() = courierLocalRepository.orderData()

    private fun updatedAt() = Single.just(timeManager.getLocalTime())

    override fun switchScreen(): Completable {
        return screenManager.saveState(FlightStatus.DCLOADING)
    }

    override fun scannerAction(scannerAction: ScannerAction) {
        scannerRepository.scannerAction(scannerAction)
    }

    override fun observeOrderData(): Flowable<CourierOrderLocalDataEntity> {
        return courierLocalRepository.observeOrderData()
            .compose(rxSchedulerFactory.applyFlowableSchedulers())
    }

    override fun deleteTask(): Completable {
        return taskId().flatMapCompletable { appRemoteRepository.deleteTask(it) }
            .compose(rxSchedulerFactory.applyCompletableSchedulers())
    }

    override fun confirmLoading(): Completable {

        return courierLocalRepository.readAllLoadingBoxes()
            .flatMap { convertToCourierTaskStatusesIntransitEntity(it) }
            .flatMapCompletable { statusesIntransit ->
                taskId().flatMapCompletable { taskId ->
//                        loaderProgress()
                    Completable.timer(3, TimeUnit.SECONDS)
                        // TODO: 16.09.2021 включить после отладки сканера
                        //appRemoteRepository.taskStatusesIntransit(taskId, statusesIntransit)
//                            .doOnComplete { loaderComplete() }
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