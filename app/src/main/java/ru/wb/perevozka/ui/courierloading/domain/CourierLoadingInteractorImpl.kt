package ru.wb.perevozka.ui.courierloading.domain

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import ru.wb.perevozka.app.PREFIX_QR_CODE
import ru.wb.perevozka.db.AppLocalRepository
import ru.wb.perevozka.db.CourierLocalRepository
import ru.wb.perevozka.db.entity.courierboxes.CourierBoxEntity
import ru.wb.perevozka.db.entity.courierlocal.CourierOrderLocalDataEntity
import ru.wb.perevozka.db.entity.flight.FlightEntity
import ru.wb.perevozka.network.api.app.AppRemoteRepository
import ru.wb.perevozka.network.api.app.FlightStatus
import ru.wb.perevozka.network.monitor.NetworkMonitorRepository
import ru.wb.perevozka.network.monitor.NetworkState
import ru.wb.perevozka.network.rx.RxSchedulerFactory
import ru.wb.perevozka.ui.scanner.domain.ScannerAction
import ru.wb.perevozka.ui.scanner.domain.ScannerRepository
import ru.wb.perevozka.utils.LogUtils
import ru.wb.perevozka.utils.managers.ScreenManager
import ru.wb.perevozka.utils.managers.TimeManager

class CourierLoadingInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val networkMonitorRepository: NetworkMonitorRepository,
    private val appRemoteRepository: AppRemoteRepository,
    private val appLocalRepository: AppLocalRepository,
    private val scannerRepository: ScannerRepository,
    private val timeManager: TimeManager,
    private val screenManager: ScreenManager,
    private val courierLocalRepository: CourierLocalRepository
) : CourierLoadingInteractor {

    private val scanLoaderProgressSubject = PublishSubject.create<CourierLoadingProgressData>()

    override fun observeNetworkConnected(): Observable<NetworkState> {
        return networkMonitorRepository.networkConnected()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    override fun scannedBoxes(): Single<List<CourierBoxEntity>> {
        return courierLocalRepository.readAllBoxes()
            .compose(rxSchedulerFactory.applySingleSchedulers())
    }

    private fun observeCourierBoxesCount(): Observable<Int> {
        return courierLocalRepository.observeBoxes()
            .toObservable()
            .map { it.size }
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    override fun observeScanProcess(): Observable<CourierLoadingProcessData> {
        return Observable.combineLatest(
            observeCourierScan(),
            observeCourierBoxesCount(),
            { scan, unloadedAndUnload -> CourierLoadingProcessData(scan, unloadedAndUnload) })
            .distinctUntilChanged()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    private fun observeCourierScan(): Observable<CourierLoadingScanBoxData> {
        return scannerRepository.observeBarcodeScanned()
            .map { parseQrCode(it) }
            .flatMapSingle { boxDefinitionResult(it) }
            .flatMap { observableStatus(it) }
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
            .find { it.id.toString() == result.parseQrCode.dstOfficeId }//2039
        return if (dstOffice == null) {
            Observable.just(CourierLoadingScanBoxData.UnknownBox)
        } else {
            courierLocalRepository.saveBox(
                CourierBoxEntity(
                    result.parseQrCode.code,
                    dstOffice.fullAddress,
                    dstOffice.id.toString(),
                    result.timeScan,
                    ""
                )
            )
                .andThen(
                    Observable.just(
                        CourierLoadingScanBoxData.BoxAdded(
                            result.parseQrCode.code,
                            dstOffice.fullAddress
                        )
                    )
                )
        }
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

    override fun scanLoaderProgress(): Observable<CourierLoadingProgressData> {
        return scanLoaderProgressSubject
    }

    override fun removeScannedBoxes(checkedBoxes: List<String>): Completable {
        return flight().flatMapCompletable { removeBoxesFromFlight(it, checkedBoxes) }
            .andThen(appLocalRepository.deleteFlightBoxesByBarcode(checkedBoxes))
            .compose(rxSchedulerFactory.applyCompletableSchedulers())
    }

    private fun removeBoxesFromFlight(flightEntity: FlightEntity, checkedBoxes: List<String>) =
        appRemoteRepository.removeBoxesFromFlight(
            flightEntity.id.toString(),
            false,
            timeManager.getOffsetLocalTime(),
            flightEntity.dc.id,
            checkedBoxes
        )


    private fun flight() = appLocalRepository.readFlight()

    private fun courierLoadingScanBoxData() = courierLocalRepository.orderData()

    private fun updatedAt() = Single.just(timeManager.getOffsetLocalTime())

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

}

data class ParseQrCode(val code: String, val dstOfficeId: String)