package ru.wb.go.ui.courierintransit.domain

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import ru.wb.go.db.CourierLocalRepository
import ru.wb.go.db.IntransitTimeRepository
import ru.wb.go.db.entity.courierlocal.LocalBoxEntity
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
import ru.wb.go.utils.managers.TimeManager

class CourierIntransitInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val networkMonitorRepository: NetworkMonitorRepository,
    private val remoteRepo: AppRemoteRepository,
    private val locRepo: CourierLocalRepository,
    private val scannerRepo: ScannerRepository,
    private val intransitTimeRepository: IntransitTimeRepository,
    private val timeManager: TimeManager,
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

    override fun setIntransitTask(orderId: String, boxes: List<LocalBoxEntity>): Completable {
        return remoteRepo.setIntransitTask(orderId, boxes)
            .doOnComplete {
                locRepo.setOnlineOffices()
            }
            .compose(rxSchedulerFactory.applyCompletableSchedulers())
    }

    override fun completeDelivery(order: LocalOrderEntity): Completable {
        return remoteRepo.taskStatusesEnd(order.orderId.toString())
            .compose(rxSchedulerFactory.applyCompletableSchedulers())
    }

    override fun clearLocalTaskData() {
        timeManager.clear()
        locRepo.clearOrder()

    }

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

    override fun getOfflineBoxes(): List<LocalBoxEntity> {
        return locRepo.getOfflineBoxes()
    }

    override fun getBoxes(): List<LocalBoxEntity> {
        return locRepo.getBoxes()
    }
}

data class CompleteDeliveryResult(val deliveredBoxes: Int, val countBoxes: Int, val cost: Int)