package ru.wb.perevozka.ui.courierintransit.domain

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import ru.wb.perevozka.app.PREFIX_QR_CODE
import ru.wb.perevozka.app.PREFIX_QR_OFFICE_CODE
import ru.wb.perevozka.db.CourierLocalRepository
import ru.wb.perevozka.db.IntransitTimeRepository
import ru.wb.perevozka.db.entity.courierboxes.CourierBoxEntity
import ru.wb.perevozka.db.entity.courierboxes.CourierIntransitGroupByOfficeEntity
import ru.wb.perevozka.network.api.app.AppRemoteRepository
import ru.wb.perevozka.network.api.app.entity.CourierTaskStatusesIntransitEntity
import ru.wb.perevozka.network.rx.RxSchedulerFactory
import ru.wb.perevozka.ui.couriermap.CourierMapAction
import ru.wb.perevozka.ui.couriermap.CourierMapState
import ru.wb.perevozka.ui.couriermap.domain.CourierMapRepository
import ru.wb.perevozka.ui.scanner.domain.ScannerRepository
import ru.wb.perevozka.ui.scanner.domain.ScannerState
import ru.wb.perevozka.utils.managers.TimeManager
import ru.wb.perevozka.utils.time.TimeFormatter

class CourierIntransitInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val appRemoteRepository: AppRemoteRepository,
    private val courierLocalRepository: CourierLocalRepository,
    private val scannerRepository: ScannerRepository,
    private val intransitTimeRepository: IntransitTimeRepository,
    private val timeManager: TimeManager,
    private val timeFormatter: TimeFormatter,
    private val courierMapRepository: CourierMapRepository,
) : CourierIntransitInteractor {

    override fun observeBoxesGroupByOffice(): Observable<List<CourierIntransitGroupByOfficeEntity>> {
        return courierLocalRepository.observeBoxesGroupByOffice()
            .flatMapSingle { list ->
                Observable.fromIterable(list).filter { it.fromCount > 0 }.toList()
            }
            .toObservable()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    override fun observeOfficeIdScanProcess(): Observable<Int> {
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
            .flatMapCompletable { intransitBoxes ->
                if (intransitBoxes.isEmpty()) {
                    // TODO: 24.09.2021 выключить для тестирования
                    //Completable.timer(2, TimeUnit.SECONDS).andThen(Completable.error(Throwable()))
                    Completable.complete()
                } else {
                    taskId().flatMapCompletable { taskId ->
                        // TODO: 24.09.2021 выключить для тестирования
//                        Completable.timer(2, TimeUnit.SECONDS).andThen(Completable.error(Throwable()))
                        taskStatusesIntransit(
                            taskId,
                            intransitBoxes
                        )
                    }
                }
            }
            .andThen(taskId().flatMapCompletable {
                // TODO: 24.09.2021 выключить для тестирования
//                Completable.timer(2, TimeUnit.SECONDS)
                taskStatusesEnd(it)
            })
            .andThen(getCompleteDeliveryResult())
            .doOnSuccess { clearData() }
            .compose(rxSchedulerFactory.applySingleSchedulers())
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

    private fun convertToCourierTaskStatusesIntransitEntity(item: List<CourierBoxEntity>) =
        Observable.fromIterable(item).map {
            with(it) {
                CourierTaskStatusesIntransitEntity(
                    id = id,
                    dstOfficeID = dstOfficeId,
                    loadingAt = loadingAt,
                    deliveredAt = if (deliveredAt.isEmpty()) null else deliveredAt
                )
            }
        }.toList()

    private fun taskId() =
        courierLocalRepository.observeOrderData()
            .map { it.courierOrderLocalEntity.id.toString() }
            .first("")

    private fun observeOfficeIdScan(): Observable<Int> {
        return scannerRepository.observeBarcodeScanned()
            .filter { it.startsWith(PREFIX_QR_OFFICE_CODE) }
            .map { parseQrCode(it) }
            .flatMap { scanOfficeId ->
                courierLoadingScanBoxData().map { it.dstOffices }
                    .map { offices -> offices.find { it.id.toString() == scanOfficeId }?.id ?: 0 }
                    .filter { it != 0 }
                    .toObservable()
            }
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    private fun parseQrCode(qrCode: String): String {
        return getSplitInfo(qrCode)[1]
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

data class CompleteDeliveryResult(val amount: Int, val unloadedCount: Int, val fromCount: Int)