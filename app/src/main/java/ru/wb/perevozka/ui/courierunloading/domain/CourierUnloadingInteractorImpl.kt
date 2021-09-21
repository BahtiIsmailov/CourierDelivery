package ru.wb.perevozka.ui.courierunloading.domain

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
import ru.wb.perevozka.db.entity.courierlocal.CourierOrderLocalDataEntity
import ru.wb.perevozka.network.api.app.AppRemoteRepository
import ru.wb.perevozka.network.api.app.FlightStatus
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

class CourierUnloadingInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val networkMonitorRepository: NetworkMonitorRepository,
    private val appRemoteRepository: AppRemoteRepository,
    private val appLocalRepository: AppLocalRepository,
    private val scannerRepository: ScannerRepository,
    private val timeManager: TimeManager,
    private val screenManager: ScreenManager,
    private val courierLocalRepository: CourierLocalRepository,
    private val taskTimerRepository: TaskTimerRepository
) : CourierUnloadingInteractor {

    companion object {
        const val DELAY_HOLD_SCANNER = 3L
        const val EMPTY_ADDRESS = ""
        const val DIVIDER_CODE = ":"
    }

    private val scanLoaderProgressSubject = PublishSubject.create<CourierUnloadingProgressData>()

    override fun nameOffice(officeId: Int): Single<String> {
        return courierLocalRepository.findOfficeById(officeId)
            .map { it.name }
            .compose(rxSchedulerFactory.applySingleSchedulers())
    }

    override fun observeNetworkConnected(): Observable<NetworkState> {
        return networkMonitorRepository.networkConnected()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    override fun readInitLastUnloadingBox(officeId: Int): Single<CourierUnloadingInitResult> {
        return Single.zip(
            courierLocalRepository.readInitLastUnloadingBox(officeId),
            courierLocalRepository.readUnloadingBoxCounter(officeId),
            { unloadingBox, unloadingCounter ->
                CourierUnloadingInitResult(
                    unloadingBox.id,
                    unloadingBox.address,
                    unloadingCounter.deliveredCount,
                    unloadingCounter.fromCount
                )
            })
            .compose(rxSchedulerFactory.applySingleSchedulers())
    }

    override fun readUnloadingBoxCounter(officeId: Int): Single<CourierUnloadingBoxCounterResult> {
        return courierLocalRepository.readUnloadingBoxCounter(officeId)
            .compose(rxSchedulerFactory.applySingleSchedulers())
    }

    override fun scannedBoxes(officeId: Int): Single<List<CourierBoxEntity>> {
        return courierLocalRepository.readAllLoadingBoxesByOfficeId(officeId)
            .compose(rxSchedulerFactory.applySingleSchedulers())
    }

    private fun observeUnloadingCounterBox(officeId: Int): Observable<CourierUnloadingBoxCounterResult> {
        return courierLocalRepository.observeUnloadingBoxCounter(officeId).toObservable()
    }

    override fun observeScanProcess(officeId: Int): Observable<CourierUnloadingProcessData> {
        return Observable.combineLatest(
            observeUnloadingScan(officeId),
            observeUnloadingCounterBox(officeId),
            { scan, boxCounter ->
                CourierUnloadingProcessData(
                    scan,
                    boxCounter.deliveredCount,
                    boxCounter.fromCount
                )
            })
            .doOnError { loaderComplete() }
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    private fun loaderProgress() {
        scanLoaderProgressSubject.onNext(CourierUnloadingProgressData.Progress)
        scannerRepository.scannerState(ScannerState.LoaderProgress)
    }

    private fun observeUnloadingScan(officeId: Int): Observable<CourierUnloadingScanBoxData> {
        return scannerRepository.observeBarcodeScanned()
            .map { parseQrCode(it) }
            .flatMapSingle { boxDefinitionResult(officeId, it) }
            .flatMap { observableStatus(it) }
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    private fun boxDefinitionResult(
        officeId: Int,
        parseQrCode: ParseQrCode
    ): Single<CourierUnloadingDefinitionResult> {
        return Single.zip(
            readAllLoadingBoxesByOfficeId(officeId),
            updatedAt(),
            { loadingBoxesByOfficeId, updatedAt ->
                CourierUnloadingDefinitionResult(
                    loadingBoxesByOfficeId,
                    parseQrCode,
                    updatedAt
                )
            }
        ).doOnError { LogUtils { logDebugApp(it.toString()) } }
            .compose(rxSchedulerFactory.applySingleSchedulers())
    }

    private fun observableStatus(result: CourierUnloadingDefinitionResult): Observable<out CourierUnloadingScanBoxData> {
        with(result) {
            val box = boxesEntity.find { it.id == parseQrCode.code }
            return if (box == null) {
                Observable.just(
                    CourierUnloadingScanBoxData.UnknownBox(
                        parseQrCode.code,
                        EMPTY_ADDRESS
                    )
                )
            } else {
                scannerRepository.scannerState(ScannerState.Stop)
                val saveAndAddedBox =
                    saveBox(timeScan, box).andThen(boxAdded(parseQrCode.code, box.address))
                val holdScanner = Observable.timer(DELAY_HOLD_SCANNER, TimeUnit.SECONDS)
                    .doOnNext { scannerRepository.scannerState(ScannerState.Start) }
                    .map { CourierUnloadingScanBoxData.ScannerReady(parseQrCode.code, box.address) }
                Observable.merge(saveAndAddedBox, holdScanner)
            }
        }
    }

    private fun saveBox(
        deliveredAt: String, box: CourierBoxEntity
    ) = courierLocalRepository.saveLoadingBox(box.copy(deliveredAt = deliveredAt))

    private fun boxAdded(
        qrcode: String, fullAddress: String
    ) = Observable.just(CourierUnloadingScanBoxData.BoxAdded(qrcode, fullAddress))

    private fun parseQrCode(qrCode: String): ParseQrCode {
        val parseParams = getSplitInfo(getInfo(qrCode))
        return ParseQrCode(parseParams[0], parseParams[1].toInt())
    }

    private fun getSplitInfo(input: String): List<String> {
        return input.split(DIVIDER_CODE)
    }

    private fun getInfo(input: String): String {
        return input.takeLast(input.length - PREFIX_QR_CODE.length)
    }

    private fun loaderComplete() {
        scanLoaderProgressSubject.onNext(CourierUnloadingProgressData.Complete)
        scannerRepository.scannerState(ScannerState.LoaderComplete)
    }

    override fun scanLoaderProgress(): Observable<CourierUnloadingProgressData> {
        return scanLoaderProgressSubject
    }

    override fun removeScannedBoxes(checkedBoxes: List<String>): Completable {
        return courierLocalRepository.deleteLoadingBoxesByQrCode(checkedBoxes)
            .compose(rxSchedulerFactory.applyCompletableSchedulers())
    }

    private fun readAllLoadingBoxesByOfficeId(officeId: Int) =
        courierLocalRepository.readAllLoadingBoxesByOfficeId(officeId)

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

    override fun confirmUnloading(officeId: Int): Completable {
        return courierLocalRepository.readAllUnloadingBoxesByOfficeId(officeId)
            .flatMap { convertToCourierTaskStatusesIntransitEntity(it) }
            .flatMapCompletable { statusesIntransit ->
                taskId().flatMapCompletable { taskId ->
//                        loaderProgress()
                    Completable.timer(3, TimeUnit.SECONDS)
                        // TODO: 16.09.2021 включить после отладки сканера
                        // TODO: 21.09.2021 добавить дату посещения ПВЗ
//                    appRemoteRepository.taskStatusesIntransit(taskId, statusesIntransit)
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

data class ParseQrCode(val code: String, val dstOfficeId: Int)

data class CourierUnloadingInitLastBoxResult(val id: String, val address: String)

data class CourierUnloadingBoxCounterResult(val deliveredCount: Int, val fromCount: Int)

data class CourierUnloadingInitResult(
    val id: String,
    val address: String,
    val deliveredCount: Int,
    val fromCount: Int
)