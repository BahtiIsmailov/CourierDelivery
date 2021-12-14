package ru.wb.go.ui.courierintransit.domain

import io.reactivex.Observable
import io.reactivex.Single
import ru.wb.go.app.PREFIX_QR_OFFICE_CODE
import ru.wb.go.app.PREFIX_QR_OFFICE_DYNAMIC_CODE
import ru.wb.go.db.CourierLocalRepository
import ru.wb.go.db.IntransitTimeRepository
import ru.wb.go.db.entity.courierboxes.CourierBoxEntity
import ru.wb.go.db.entity.courierboxes.CourierIntransitGroupByOfficeEntity
import ru.wb.go.network.api.app.AppRemoteRepository
import ru.wb.go.network.api.app.entity.CourierTaskStatusesIntransitEntity
import ru.wb.go.network.monitor.NetworkMonitorRepository
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.network.rx.RxSchedulerFactory
import ru.wb.go.ui.couriermap.CourierMapAction
import ru.wb.go.ui.couriermap.CourierMapState
import ru.wb.go.ui.couriermap.domain.CourierMapRepository
import ru.wb.go.ui.scanner.domain.ScannerRepository
import ru.wb.go.ui.scanner.domain.ScannerState
import ru.wb.go.utils.LogUtils
import ru.wb.go.utils.managers.TimeManager
import ru.wb.go.utils.time.TimeFormatter

class CourierIntransitInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val networkMonitorRepository: NetworkMonitorRepository,
    private val appRemoteRepository: AppRemoteRepository,
    private val courierLocalRepository: CourierLocalRepository,
    private val scannerRepository: ScannerRepository,
    private val intransitTimeRepository: IntransitTimeRepository,
    private val timeManager: TimeManager,
    private val timeFormatter: TimeFormatter,
    private val courierMapRepository: CourierMapRepository,
) : CourierIntransitInteractor {

    companion object {
        const val DEFAULT_OFFICE = "0"
    }

    override fun observeNetworkConnected(): Observable<NetworkState> {
        return networkMonitorRepository.networkConnected()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    override fun observeBoxesGroupByOffice(): Observable<List<CourierIntransitGroupByOfficeEntity>> {
        return courierLocalRepository.observeBoxesGroupByOffice()
            .flatMapSingle { list ->
                Observable.fromIterable(list).filter { it.fromCount > 0 }.toList()
            }
            .toObservable()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    override fun observeOfficeIdScanProcess(): Observable<CourierIntransitScanOfficeData> {
        return observeOfficeIdScan()
    }

    override fun scannerAction(scannerAction: ScannerState) {
        scannerRepository.scannerState(scannerAction)
    }

    override fun startTime(): Observable<Long> {
        var offsetSec = 0L
        // TODO: 25.11.2021 переработать с учетом часового пояса
        val startedTaskTime =
            timeFormatter.dateTimeWithoutTimezoneFromString(timeManager.getStartedTaskTime()).millis
        if (startedTaskTime != 0L) {
            val currentTime = timeFormatter.currentDateTime().millis
            offsetSec = kotlin.math.abs(currentTime - startedTaskTime) / 1000
        }
        return intransitTimeRepository.startTimer()
            .toObservable()
            .map { it + offsetSec }
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    private fun getCompleteDeliveryResult(): Single<CompleteDeliveryResult> {
        return courierLocalRepository.completeDeliveryResult()
    }

    override fun completeDelivery(): Single<CompleteDeliveryResult> {
        val notUnloadingBoxes = readNotUnloadingBoxes()
        val loadingConvertBoxes = convertToCourierTaskStatusesIntransitEntity(notUnloadingBoxes)


        return sendIntransitBoxes(loadingConvertBoxes)
            .andThen(taskToEnd())
            .andThen(getCompleteDeliveryResult())
            .compose(rxSchedulerFactory.applySingleSchedulers())
    }

    private fun readNotUnloadingBoxes() = courierLocalRepository.readNotUnloadingBoxes()


    override fun confirmDeliveryComplete() {
        clearData()
    }

    private fun sendIntransitBoxes(intransitBoxes: List<CourierTaskStatusesIntransitEntity>) =
//        if (intransitBoxes.isEmpty()) {
//            // TODO: 24.09.2021 выключить для тестирования
//            //Completable.timer(2, TimeUnit.SECONDS).andThen(Completable.error(Throwable()))
//            Completable.complete()
//        } else {
//            taskId().flatMapCompletable { taskId ->
//                // TODO: 24.09.2021 выключить для тестирования
//                // Completable.timer(2, TimeUnit.SECONDS).andThen(Completable.error(Throwable()))
//                taskStatusesIntransit(taskId, intransitBoxes)
//            }
//        }

        // TODO: 24.09.2021 для тестирования (всегда отправляем коробки на бэк)
        taskId().flatMapCompletable { taskId -> taskStatusesIntransit(taskId, intransitBoxes) }

    private fun taskToEnd() = taskId().flatMapCompletable {
        // TODO: 24.09.2021 выключить для тестирования
        // Completable.timer(2, TimeUnit.SECONDS)
        taskStatusesEnd(it)
    }

//    override fun forcedCompleteDelivery(): Single<CompleteDeliveryResult> {
//        return courierLocalRepository.readAllLoadingBoxesSync()
//            .flatMap {
//                convertToForcedCourierTaskStatusesIntransitEntity(
//                    it,
//                    timeManager.getLocalTime()
//                )
//            }
//            .flatMapCompletable { intransitBoxes ->
//                taskId().flatMapCompletable { taskId ->
//                    taskStatusesIntransit(taskId, intransitBoxes)
//                }
//            }
//            .andThen(taskId().flatMapCompletable { taskStatusesEnd(it) })
//            .andThen(getCompleteDeliveryResult())
//            .doOnSuccess { clearData() }
//            .compose(rxSchedulerFactory.applySingleSchedulers())
//    }

    private fun clearData() {
        timeManager.clear()
        courierLocalRepository.deleteAllWarehouse()
        courierLocalRepository.deleteAllOrder()
        courierLocalRepository.deleteAllOrderOffices()
        courierLocalRepository.deleteAllLoadingBoxes()
        courierLocalRepository.deleteAllVisitedOffices()
    }

    private fun taskStatusesEnd(taskId: String) = appRemoteRepository.taskStatusesEnd(taskId)
        .compose(rxSchedulerFactory.applyCompletableSchedulers())

    private fun taskStatusesIntransit(
        taskId: String,
        intransitBoxes: List<CourierTaskStatusesIntransitEntity>
    ) = appRemoteRepository.taskStatusesIntransit(taskId, intransitBoxes)
        .compose(rxSchedulerFactory.applyCompletableSchedulers())

    private fun convertToCourierTaskStatusesIntransitEntity(boxes: List<CourierBoxEntity>): List<CourierTaskStatusesIntransitEntity> {

        val convertItems = mutableListOf<CourierTaskStatusesIntransitEntity>()
        boxes.forEach {
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

//    private fun convertToForcedCourierTaskStatusesIntransitEntity(
//        item: List<CourierBoxEntity>,
//        localTime: String
//    ) =
//        Observable.fromIterable(item).map {
//            with(it) {
//                CourierTaskStatusesIntransitEntity(
//                    id = id,
//                    dstOfficeID = dstOfficeId,
//                    loadingAt = loadingAt, //if (loadingAt.isEmpty()) localTime else
//                    deliveredAt = localTime
//                )
//            }
//        }.toList()
//            .map {
//            it.add(
//                CourierTaskStatusesIntransitEntity(
//                    id = "680689061",
//                    dstOfficeID = 3091,
//                    loadingAt = "2021-11-29T20:42:52.375+03:00",
//                    deliveredAt = localTime
//                )
//            )
//            it
//        }

    override fun taskId(): Single<String> =
        courierLocalRepository.observeOrderData()
            .map { it.courierOrderLocalEntity.id.toString() }
            .first("")
            .compose(rxSchedulerFactory.applySingleSchedulers())

    private fun observeOfficeIdScan(): Observable<CourierIntransitScanOfficeData> {
        return scannerRepository.observeBarcodeScanned()
            .doOnNext { LogUtils { logDebugApp("CourierIntransitInteractorImpl scannerRepository.observeBarcodeScanned() " + it) } }
            .map { parseOfficeIdByQrCode(it) }
            .flatMap { scanOfficeId ->
                when (scanOfficeId) {
                    is ParseQrOfficeResult.Office -> {
                        courierLoadingScanBoxData()
                            .map { it.dstOffices }
                            .map { offices ->
                                val officeId = scanOfficeId.office.toInt()
                                if (offices.find { it.id == officeId } == null) {
                                    CourierIntransitScanOfficeData.UnknownOffice
                                } else {
                                    CourierIntransitScanOfficeData.NecessaryOffice(officeId)
                                }
                            }

                    }
                    ParseQrOfficeResult.Failed -> {
                        Single.just(CourierIntransitScanOfficeData.UnknownOffice)
                    }
                }
                    .toObservable()
            }
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    private fun parseOfficeIdByQrCode(qrCode: String): ParseQrOfficeResult {
        return if (qrCode.startsWith(PREFIX_QR_OFFICE_CODE))
            getSplitInfo(qrCode)
        else if (qrCode.startsWith(PREFIX_QR_OFFICE_DYNAMIC_CODE)) {
            getSplitDynamicOfficeInfo(qrCode)
        } else ParseQrOfficeResult.Failed
    }

    private fun getSplitInfo(input: String): ParseQrOfficeResult {
        return ParseQrOfficeResult.Office(input.split(".")[1])
    }

    private fun getSplitDynamicOfficeInfo(input: String): ParseQrOfficeResult {
        val splitter = input.split(";")
        val officeId = splitter[0].replace("o:", "")
        val officeHash = splitter[1].replace("c:", "")
        val currentOfficeHash = getHashByOfficeId(officeId.toInt())
        return if (currentOfficeHash == officeHash) ParseQrOfficeResult.Office(officeId)
        else ParseQrOfficeResult.Failed
    }

    private fun getHashByOfficeId(officeId: Int): String {
        val startTime =
            timeFormatter.dateTimeFromStringSimple("2020-01-01T00:00:00.000+03:00").millis
        val currentTime = timeFormatter.currentDateTime().millis
        val dif = (currentTime - startTime) / 86400 / 1000
        val m = (31 * dif * officeId).toString()
        return m.takeLast(4)
    }

    private fun courierLoadingScanBoxData() = courierLocalRepository.orderDataSync()

    override fun observeMapAction(): Observable<CourierMapAction> {
        return courierMapRepository.observeMapAction()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    override fun mapState(state: CourierMapState) {
        courierMapRepository.mapState(state)
    }

}

sealed class ParseQrOfficeResult {
    object Failed : ParseQrOfficeResult()
    data class Office(val office: String) : ParseQrOfficeResult()
}

data class CompleteDeliveryResult(val unloadedCount: Int, val fromCount: Int)