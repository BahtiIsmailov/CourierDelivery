package ru.wb.go.ui.courierunloading.domain

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import ru.wb.go.app.PREFIX_QR_CODE
import ru.wb.go.db.CourierLocalRepository
import ru.wb.go.db.entity.courierboxes.CourierBoxEntity
import ru.wb.go.db.entity.courierlocal.CourierOrderLocalDataEntity
import ru.wb.go.db.entity.courierlocal.CourierOrderVisitedOfficeLocalEntity
import ru.wb.go.network.api.app.AppRemoteRepository
import ru.wb.go.network.api.app.entity.CourierTaskStatusesIntransitEntity
import ru.wb.go.network.monitor.NetworkMonitorRepository
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.network.rx.RxSchedulerFactory
import ru.wb.go.ui.scanner.domain.ScannerRepository
import ru.wb.go.ui.scanner.domain.ScannerState
import ru.wb.go.utils.LogUtils
import ru.wb.go.utils.managers.TimeManager
import java.util.concurrent.TimeUnit

class CourierUnloadingInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val networkMonitorRepository: NetworkMonitorRepository,
    private val appRemoteRepository: AppRemoteRepository,
    private val scannerRepository: ScannerRepository,
    private val timeManager: TimeManager,
    private val courierLocalRepository: CourierLocalRepository,
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

    override fun readUnloadingLastBox(officeId: Int): Single<CourierUnloadingLastBoxResult> {
        return Single.zip(
            courierLocalRepository.readInitLastUnloadingBox(officeId),
            courierLocalRepository.readUnloadingBoxCounter(officeId),
            { unloadingBox, unloadingCounter ->
                CourierUnloadingLastBoxResult(
                    unloadingBox.id,
                    unloadingBox.address,
                    unloadingCounter.unloadedCount,
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

    private fun getUnloadingCounterBox(officeId: Int): Single<CourierUnloadingBoxCounterResult> {
        return courierLocalRepository.observeUnloadingBoxCounter(officeId).firstOrError()
    }

    override fun observeScanProcess(officeId: Int): Observable<CourierUnloadingProcessData> {
        return observeUnloadingScan(officeId).flatMapSingle { scan ->
            getUnloadingCounterBox(officeId).map { boxCounter ->
                var tmpScan = scan
                if (boxCounter.unloadedCount == boxCounter.fromCount && scan is CourierUnloadingScanBoxData.ScannerReady) {
                    tmpScan =
                        CourierUnloadingScanBoxData.UnloadingCompleted(scan.qrCode, scan.address)
                }
                CourierUnloadingProcessData(
                    tmpScan,
                    boxCounter.unloadedCount,
                    boxCounter.fromCount
                )
            }
        }
            .doOnError { loaderComplete() }
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    private fun loaderProgress() {
        scanLoaderProgressSubject.onNext(CourierUnloadingProgressData.Progress)
        scannerRepository.scannerState(ScannerState.LoaderProgress)
    }

    private fun observeUnloadingScan(officeId: Int): Observable<CourierUnloadingScanBoxData> {
        return scannerRepository.observeBarcodeScanned()
            .doOnNext { LogUtils { logDebugApp("scannerRepository.observeBarcodeScanned() " + it) } }
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
        return ParseQrCode(parseParams[0], parseParams[1].trim().toInt())
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

    override fun scannerAction(scannerAction: ScannerState) {
        scannerRepository.scannerState(scannerAction)
    }

    override fun observeOrderData(): Flowable<CourierOrderLocalDataEntity> {
        return courierLocalRepository.observeOrderData()
            .compose(rxSchedulerFactory.applyFlowableSchedulers())
    }

    override fun confirmUnloading(officeId: Int): Completable {
        insertVisitedAtOffice(officeId, false)
        val notUnloadingBoxes = readNotUnloadingBoxes()
        val loadingConvertBoxes = convertToCourierTaskStatusesIntransitEntity(notUnloadingBoxes)
        val taskId = taskId()
        return taskStatusesIntransit(loadingConvertBoxes, taskId)
            .compose(rxSchedulerFactory.applyCompletableSchedulers())
    }

    override fun confirmUnloadingComplete(officeId: Int) {
        loaderComplete()
        insertVisitedAtOffice(officeId, true)
        insertAllVisitedOffice()
    }

    private fun taskStatusesIntransit(
        statusesIntransit: List<CourierTaskStatusesIntransitEntity>,
        taskId: String
    ) = appRemoteRepository.taskStatusesIntransit(taskId, statusesIntransit)

    private fun readNotUnloadingBoxes() = courierLocalRepository.readNotUnloadingBoxes()

    private fun insertVisitedAtOffice(officeId: Int, isUnloaded: Boolean) {
        val courierOrderVisitedOfficeLocalEntity = CourierOrderVisitedOfficeLocalEntity(
            dstOfficeId = officeId,
            visitedAt = timeManager.getLocalTime(),
            isUnload = isUnloaded
        )
        return courierLocalRepository.insertVisitedOffice(courierOrderVisitedOfficeLocalEntity)
    }

    private fun insertAllVisitedOfficeSync() = courierLocalRepository.insertAllVisitedOfficeSync()

    private fun insertAllVisitedOffice() = courierLocalRepository.insertAllVisitedOffice()

    private fun convertToCourierTaskStatusesIntransitEntity(items: List<CourierBoxEntity>): List<CourierTaskStatusesIntransitEntity> {
        val convertItems = mutableListOf<CourierTaskStatusesIntransitEntity>()
        items.forEach {
            val convertItem = with(it) {
                CourierTaskStatusesIntransitEntity(
                    id = id,
                    dstOfficeID = dstOfficeId,
                    loadingAt = loadingAt,
                    deliveredAt = if (deliveredAt.isEmpty()) null else deliveredAt
                )
            }
            convertItems.add(convertItem)
        }
        return convertItems
    }

    private fun taskId() = courierLocalRepository.orderData().courierOrderLocalEntity.id.toString()

    private fun taskIdSync() =
        courierLocalRepository.observeOrderData()
            .map { it.courierOrderLocalEntity.id.toString() }
            .first("")

}

data class ParseQrCode(val code: String, val dstOfficeId: Int)

data class CourierUnloadingInitLastBoxResult(val id: String, val address: String)

data class CourierUnloadingBoxCounterResult(val unloadedCount: Int, val fromCount: Int)

data class CourierUnloadingLastBoxResult(
    val id: String,
    val address: String,
    val deliveredCount: Int,
    val fromCount: Int
)