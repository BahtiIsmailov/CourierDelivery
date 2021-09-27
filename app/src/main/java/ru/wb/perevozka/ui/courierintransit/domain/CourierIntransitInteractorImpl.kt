package ru.wb.perevozka.ui.courierintransit.domain

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import ru.wb.perevozka.app.PREFIX_QR_CODE
import ru.wb.perevozka.db.CourierLocalRepository
import ru.wb.perevozka.db.IntransitTimeRepository
import ru.wb.perevozka.db.entity.courierboxes.CourierBoxEntity
import ru.wb.perevozka.db.entity.courierboxes.CourierIntransitGroupByOfficeEntity
import ru.wb.perevozka.network.api.app.AppRemoteRepository
import ru.wb.perevozka.network.api.app.entity.CourierTaskStatusesIntransitEntity
import ru.wb.perevozka.network.rx.RxSchedulerFactory
import ru.wb.perevozka.ui.scanner.domain.ScannerRepository
import ru.wb.perevozka.ui.scanner.domain.ScannerState
import ru.wb.perevozka.utils.managers.TimeManager
import ru.wb.perevozka.utils.time.TimeFormatter
import java.util.concurrent.TimeUnit

class CourierIntransitInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val appRemoteRepository: AppRemoteRepository,
    private val courierLocalRepository: CourierLocalRepository,
    private val scannerRepository: ScannerRepository,
    private val intransitTimeRepository: IntransitTimeRepository,
    private val timeManager: TimeManager,
    private val timeFormatter: TimeFormatter
) : CourierIntransitInteractor {

    override fun observeBoxesGroupByOffice(): Flowable<List<CourierIntransitGroupByOfficeEntity>> {
        return courierLocalRepository.observeBoxesGroupByOffice()
            .compose(rxSchedulerFactory.applyFlowableSchedulers())
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

    override fun completeDelivery(): Completable {
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
            .doOnComplete { clearData() }
            .compose(rxSchedulerFactory.applyCompletableSchedulers())
    }

    private fun clearData() {
        timeManager.clear()
        courierLocalRepository.deleteAllCurrentWarehouse()
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
                    deliveredAt = deliveredAt
                )
            }
        }.toList()

    private fun taskId() =
        courierLocalRepository.observeOrderData()
            .map { it.courierOrderLocalEntity.id.toString() }
            .first("")

    private fun observeOfficeIdScan(): Observable<Int> {
        return scannerRepository.observeBarcodeScanned()
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
        return getSplitInfo(getInfo(qrCode))[0]
    }

    private fun getSplitInfo(input: String): List<String> {
        return input.split(":")
    }

    private fun getInfo(input: String): String {
        return input.takeLast(input.length - PREFIX_QR_CODE.length)
    }

    private fun courierLoadingScanBoxData() = courierLocalRepository.orderData()

}