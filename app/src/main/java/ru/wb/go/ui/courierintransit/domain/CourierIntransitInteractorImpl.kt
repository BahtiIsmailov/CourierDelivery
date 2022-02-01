package ru.wb.go.ui.courierintransit.domain

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import ru.wb.go.db.CourierLocalRepository
import ru.wb.go.db.IntransitTimeRepository
import ru.wb.go.db.entity.courierlocal.LocalOfficeEntity
import ru.wb.go.db.entity.courierlocal.LocalOrderEntity
import ru.wb.go.network.api.app.AppRemoteRepository
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
    private val remoteRepo: AppRemoteRepository,
    private val locRepo: CourierLocalRepository,
    private val scannerRepo: ScannerRepository,
    private val intransitTimeRepository: IntransitTimeRepository,
    private val timeManager: TimeManager,
    private val timeFormatter: TimeFormatter,
    private val courierMapRepository: CourierMapRepository,
) : CourierIntransitInteractor {

    override fun observeNetworkConnected(): Observable<NetworkState> {
        return networkMonitorRepository.networkConnected()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    override fun getOffices(): Observable<List<LocalOfficeEntity>> {
        return locRepo.getOfficesFlowable()
            .toObservable()
            .map { office ->
                office.toMutableList().sortedWith(
                    compareBy({ it.isVisited }, { it.deliveredBoxes == it.countBoxes })
                )
            }
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    override fun observeOfficeIdScanProcess(): Observable<CourierIntransitScanOfficeData> {
        return scannerRepo.observeBarcodeScanned()
            .doOnNext { LogUtils { logDebugApp("CourierIntransitInteractorImpl scannerRepository.observeBarcodeScanned() $it") } }
            .map { scannerRepo.parseScanOfficeQr(it) }
            .flatMap { parse ->
                when (parse.isOk) {
                    true -> {
                        Single.just(locRepo.getOffices())
                            .map { offices ->
                                if (offices.find { it.officeId == parse.officeId } == null) {
                                    CourierIntransitScanOfficeData.WrongOffice
                                } else {
                                    locRepo.visitOffice(parse.officeId)
                                    CourierIntransitScanOfficeData.NecessaryOffice(parse.officeId)
                                }
                            }

                    }
                    else -> {
                        Single.just(CourierIntransitScanOfficeData.UnknownQrOffice)
                    }
                }
                    .toObservable()
            }
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    override fun scannerAction(scannerAction: ScannerState) {
        scannerRepo.scannerState(scannerAction)
    }

    override fun initOrderTimer(): Observable<Long> {
        val order = locRepo.getOrder()!!
        // TODO: 25.11.2021 переработать с учетом часового пояса
        val offsetSec = timeManager.getPassedTime(order.startedAt)

        return intransitTimeRepository.startTimer()
            .toObservable()
            .map { it + offsetSec }
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    override fun completeDelivery(): Single<CompleteDeliveryResult> {

        val boxes = locRepo.getBoxes()
        val order = locRepo.getOrder()!!
        val orderId = order.orderId.toString()

        return remoteRepo.setIntransitTask(orderId, boxes)
            .doOnComplete {
                locRepo.setOnlineOffices()
            }
            .andThen(
                taskStatusesEnd(orderId)
                    .toSingle { CompleteDeliveryResult(boxes.size, boxes.size, order.cost) }
            )
            .compose(rxSchedulerFactory.applySingleSchedulers())
    }

    override fun clearLocalTaskData() {
        timeManager.clear()
        locRepo.clearOrder()

    }

    private fun taskStatusesEnd(taskId: String) = remoteRepo.taskStatusesEnd(taskId)
        .compose(rxSchedulerFactory.applyCompletableSchedulers())

    override fun getOrder(): LocalOrderEntity {
        return locRepo.getOrder()!!
    }

    override fun getOrderId(): Single<String> {
        return locRepo.getOrderId()
    }

    override fun observeMapAction(): Observable<CourierMapAction> {
        return courierMapRepository.observeMapAction()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    override fun mapState(state: CourierMapState) {
        courierMapRepository.mapState(state)
    }

    override fun getOfflineBoxes(): Int {
        val boxes = locRepo.getOfflineBoxes()
        return boxes.size
    }
}

data class CompleteDeliveryResult(val deliveredBoxes: Int, val countBoxes: Int, val cost: Int)