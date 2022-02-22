package ru.wb.go.ui.courierintransitofficescanner.domain

import io.reactivex.Observable
import io.reactivex.Single
import ru.wb.go.db.CourierLocalRepository
import ru.wb.go.db.entity.courierlocal.LocalOfficeEntity
import ru.wb.go.network.monitor.NetworkMonitorRepository
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.network.rx.RxSchedulerFactory
import ru.wb.go.ui.scanner.domain.ScannerRepository
import ru.wb.go.ui.scanner.domain.ScannerState

class CourierIntransitOfficeScannerInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val networkMonitorRepository: NetworkMonitorRepository,
    private val locRepo: CourierLocalRepository,
    private val scannerRepo: ScannerRepository,
) : CourierIntransitOfficeScannerInteractor {

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

    override fun observeOfficeIdScanProcess(): Observable<CourierIntransitOfficeScanData> {
        return scannerRepo.observeBarcodeScanned()
            .map { scannerRepo.parseScanOfficeQr(it) }
            .flatMap { parse ->
                when (parse.isOk) {
                    true -> {
                        Single.just(locRepo.getOffices())
                            .map { offices ->
                                if (offices.find { it.officeId == parse.officeId } == null) {
                                    CourierIntransitOfficeScanData.WrongOfficeScan
                                } else {
                                    locRepo.visitOffice(parse.officeId)
                                    CourierIntransitOfficeScanData.NecessaryOfficeScan(parse.officeId)
                                }
                            }
                    }
                    else -> {
                        Single.just(CourierIntransitOfficeScanData.UnknownQrOfficeScan)
                    }
                }
                    .toObservable()
            }
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    override fun scannerAction(scannerAction: ScannerState) {
        scannerRepo.scannerState(scannerAction)
    }

}
