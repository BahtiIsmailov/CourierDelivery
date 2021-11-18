package ru.wb.go.ui.courierintransit.domain

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import ru.wb.go.app.PREFIX_QR_OFFICE_CODE
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
        val startedTaskTime =
            timeFormatter.dateTimeWithoutTimezoneFromString(timeManager.getStartedTaskTime()).millis
        if (startedTaskTime != 0L) {
            val currentTime = timeFormatter.currentDateTime().millis
            offsetSec = (currentTime - startedTaskTime) / 1000
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
        return courierLocalRepository.readNotUnloadingBoxes()
            .flatMap { convertToCourierTaskStatusesIntransitEntity(it) }
            .flatMapCompletable { sendIntransitBoxes(it) }
            .andThen(taskToEnd())
            .andThen(getCompleteDeliveryResult())
            .doOnSuccess { clearData() }
            .compose(rxSchedulerFactory.applySingleSchedulers())
    }

    private fun sendIntransitBoxes(intransitBoxes: List<CourierTaskStatusesIntransitEntity>) =
        if (intransitBoxes.isEmpty()) {
            // TODO: 24.09.2021 выключить для тестирования
            //Completable.timer(2, TimeUnit.SECONDS).andThen(Completable.error(Throwable()))
            Completable.complete()
        } else {
            taskId().flatMapCompletable { taskId ->
                // TODO: 24.09.2021 выключить для тестирования
                // Completable.timer(2, TimeUnit.SECONDS).andThen(Completable.error(Throwable()))
                taskStatusesIntransit(taskId, intransitBoxes)
            }
        }

    private fun taskToEnd() = taskId().flatMapCompletable {
        // TODO: 24.09.2021 выключить для тестирования
        // Completable.timer(2, TimeUnit.SECONDS)
        taskStatusesEnd(it)
    }

    private fun clearData() {
        timeManager.clear()
        courierLocalRepository.deleteAllWarehouse()
        courierLocalRepository.deleteAllOrder()
        courierLocalRepository.deleteAllOrderOffices()
        courierLocalRepository.deleteAllLoadingBoxes()
        courierLocalRepository.deleteAllVisitedOffices()
    }

    private fun taskStatusesEnd(taskId: String) = appRemoteRepository.taskStatusesEnd(taskId)

    private fun taskStatusesIntransit(
        taskId: String,
        intransitBoxes: List<CourierTaskStatusesIntransitEntity>
    ) = appRemoteRepository.taskStatusesIntransit(taskId, intransitBoxes)

    private fun convertToCourierTaskStatusesIntransitEntity(boxes: List<CourierBoxEntity>) =
        Observable.fromIterable(boxes).map {
            with(it) {
                CourierTaskStatusesIntransitEntity(
                    id = id,
                    dstOfficeID = dstOfficeId,
                    loadingAt = loadingAt,
                    deliveredAt = if (deliveredAt.isEmpty()) null else deliveredAt
                )
            }
        }.toList()

    override fun taskId(): Single<String> =
        courierLocalRepository.observeOrderData()
            .map { it.courierOrderLocalEntity.id.toString() }
            .first("")
            .compose(rxSchedulerFactory.applySingleSchedulers())

    private fun observeOfficeIdScan(): Observable<CourierIntransitScanOfficeData> {
        return scannerRepository.observeBarcodeScanned()
//            .filter { it.startsWith(PREFIX_QR_OFFICE_CODE) }
            .map { parseQrCode(it) }
            .flatMap { scanOfficeId ->
                courierLoadingScanBoxData().map { it.dstOffices }
                    .map { offices -> offices.find { it.id.toString() == scanOfficeId }?.id ?: 0 }
                    .map {
                        if (it == 0) CourierIntransitScanOfficeData.UnknownOffice
                        else CourierIntransitScanOfficeData.Office(it)
                    }
                    .toObservable()
            }
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    private fun parseQrCode(qrCode: String): String {
        return if (qrCode.startsWith(PREFIX_QR_OFFICE_CODE)) getSplitInfo(qrCode)[1] else "0"
    }

    private fun getSplitInfo(input: String): List<String> {
        return input.split(".")
    }

    private fun courierLoadingScanBoxData() = courierLocalRepository.orderData()

    override fun observeMapAction(): Observable<CourierMapAction> {
        return courierMapRepository.observeMapAction()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    override fun mapState(state: CourierMapState) {
        courierMapRepository.mapState(state)
    }

}

data class CompleteDeliveryResult(val unloadedCount: Int, val fromCount: Int)