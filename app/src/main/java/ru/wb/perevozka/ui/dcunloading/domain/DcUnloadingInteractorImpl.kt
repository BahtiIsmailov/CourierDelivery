package ru.wb.perevozka.ui.dcunloading.domain

import ru.wb.perevozka.db.AppLocalRepository
import ru.wb.perevozka.db.Optional
import ru.wb.perevozka.db.entity.dcunloadedboxes.DcReturnHandleBarcodeEntity
import ru.wb.perevozka.db.entity.dcunloadedboxes.DcUnloadingBarcodeEntity
import ru.wb.perevozka.db.entity.flighboxes.FlightBoxEntity
import ru.wb.perevozka.db.entity.flighboxes.ScanProcessStatus
import ru.wb.perevozka.network.api.app.AppRemoteRepository
import ru.wb.perevozka.network.api.app.FlightStatus
import ru.wb.perevozka.network.exceptions.BadRequestException
import ru.wb.perevozka.network.monitor.NetworkMonitorRepository
import ru.wb.perevozka.network.monitor.NetworkState
import ru.wb.perevozka.network.rx.RxSchedulerFactory
import ru.wb.perevozka.ui.dcloading.domain.ScanProgressData
import ru.wb.perevozka.ui.scanner.domain.ScannerState
import ru.wb.perevozka.ui.scanner.domain.ScannerRepository
import ru.wb.perevozka.utils.managers.ScreenManager
import ru.wb.perevozka.utils.managers.TimeManager
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject

class DcUnloadingInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val networkMonitorRepository: NetworkMonitorRepository,
    private val appRemoteRepository: AppRemoteRepository,
    private val appLocalRepository: AppLocalRepository,
    private val scannerRepository: ScannerRepository,
    private val timeManager: TimeManager,
    private val screenManager: ScreenManager,
) : DcUnloadingInteractor {

    private val barcodeManualInput = PublishSubject.create<Pair<String, Boolean>>()

    private val barcodeScannedSubject = PublishSubject.create<String>()

    private val scanLoaderProgressSubject = PublishSubject.create<ScanProgressData>()

    override fun barcodeManualInput(barcode: String) {
        barcodeManualInput.onNext(Pair(barcode, true))
    }

    override fun observeUnloadingProcess(): Observable<DcUnloadingData> {
        return Observable.combineLatest(
            observeScanProcess(),
            observeUnloadingCounterBoxes(),
            { scan, unloadedAndUnload -> DcUnloadingData(scan, unloadedAndUnload) })
            .distinctUntilChanged()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    override fun scanLoaderProgress(): Observable<ScanProgressData> {
        return scanLoaderProgressSubject
    }

    private fun barcodeScannerInput(): Observable<Pair<String, Boolean>> {
        return scannerRepository.observeBarcodeScanned(barcodeScannedSubject)
            .map { Pair(it, false) }
    }

    private fun observeUnloadingCounterBoxes(): Observable<DcUnloadingCounterEntity> {
        return appLocalRepository.observeDcUnloadingCounter()
            .toObservable()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    private fun warehouseScanOptional(dcBoxDefinitionResult: DcBoxDefinitionResult): Observable<DcBoxDefinitionResult> {
        return with(dcBoxDefinitionResult) {
            unloadBoxFromBalanceRemote(flight.id.toString(),
                barcode,
                isManual,
                updatedAt,
                flight.dc.id)
                .map { dcBoxDefinitionResult.copy(warehouseScanOptional = Optional.Success(it)) }
                .onErrorReturn {
                    if (it is BadRequestException) {
                        dcBoxDefinitionResult.copy(
                            warehouseScanOptional = Optional.Empty(),
                            codeError = it.error.code)
                    } else throw it
                }
        }
            .toObservable()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    private fun saveBoxToBalanceByWarehouse(
        barcode: String, flightBoxEntity: FlightBoxEntity, updatedAt: String,
    ): Observable<DcUnloadingAction> {
        val saveFlightBox = saveFlightBox(flightBoxEntity.copy(updatedAt = updatedAt))
        val removeDeliveryErrorBox = removeDeliveryErrorBox(barcode)
        val boxUnloaded = Single.just(DcUnloadingAction.BoxUnloaded(barcode))
        return saveFlightBox
            .andThen(removeDeliveryErrorBox)
            .andThen(boxUnloaded)
            .toObservable()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    private fun saveFlightBox(attachedBoxEntity: FlightBoxEntity) =
        appLocalRepository.saveFlightBox(attachedBoxEntity)

    private fun removeDeliveryErrorBox(barcode: String) =
        appLocalRepository.deleteDeliveryErrorBoxByBarcode(barcode)


    private fun observeScanProcess(): Observable<DcUnloadingAction> {
        return Observable.merge(barcodeManualInput, barcodeScannerInput())
            .flatMapSingle {
                Completable.fromAction {
                    scanLoaderProgressSubject.onNext(ScanProgressData.Progress)
                    scannerRepository.scannerState(ScannerState.LoaderProgress)
                }.andThen(Single.just(it))
            }
            .flatMapSingle { boxDefinitionResult(it.first, it.second) }
            .flatMap { warehouseScanOptional(it) }
            .flatMap { boxDefinition ->
                val warehouseScanOptional = boxDefinition.warehouseScanOptional
                val barcode = boxDefinition.barcode
                val updatedAt = timeManager.getOffsetLocalTime()
                val codeError = boxDefinition.codeError

                return@flatMap when (warehouseScanOptional) {
                    is Optional.Success -> { //информация по коробке получена
                        saveBoxToBalanceByWarehouse(barcode, warehouseScanOptional.data, updatedAt)
                    }
                    is Optional.Empty -> { //запрос завершился с 400 кодом - определяем код ошибки
                        when (codeError) {
                            ScanProcessStatus.BOX_DOES_NOT_FIT_FLIGHT.name ->
                                Observable.just(DcUnloadingAction.BoxDoesNotBelongFlight)
                            ScanProcessStatus.BOX_INFO_DOES_NOT_EXIST.name ->
                                Observable.just(DcUnloadingAction.BoxDoesNotBelongInfoEmpty(barcode))
                            ScanProcessStatus.BOX_NOT_FROM_THIS_WAREHOUSE.name ->
                                Observable.just(DcUnloadingAction.BoxDoesNotBelongDc)
                            else -> Observable.just(DcUnloadingAction.BoxDoesNotBelongInfoEmpty(
                                barcode))
                        }
                    }

                }
            }
            .flatMap { Completable.fromAction { loaderComplete() }.andThen(Observable.just(it)) }
            .doOnError { loaderComplete() }
            .startWith(Observable.just(DcUnloadingAction.Init))
    }

    private fun loaderComplete() {
        scanLoaderProgressSubject.onNext(ScanProgressData.Complete)
        scannerRepository.scannerState(ScannerState.LoaderComplete)
    }

    override fun findDcUnloadedHandleBoxes(): Single<List<DcReturnHandleBarcodeEntity>> {
        return flight().map { it.dc.id }
            .flatMap { currentOfficeId -> appLocalRepository.findDcReturnHandleBoxes(currentOfficeId) }
            .compose(rxSchedulerFactory.applySingleSchedulers())
    }

    override fun findDcUnloadedListBoxes(): Single<List<DcUnloadingBarcodeEntity>> {
        return flight().map { it.dc.id }
            .flatMap { currentOfficeId -> appLocalRepository.findDcUnloadedBarcodes(currentOfficeId) }
            .compose(rxSchedulerFactory.applySingleSchedulers())
    }

    private fun boxDefinitionResult(
        barcode: String,
        isManual: Boolean,
    ): Single<DcBoxDefinitionResult> {
        return flight().map { flight ->
            val updatedAt = timeManager.getOffsetLocalTime()
            val warehouseScanOptional = Optional.Empty<FlightBoxEntity>()
            DcBoxDefinitionResult(flight, warehouseScanOptional, barcode, isManual, updatedAt, "")
        }
    }

    private fun flight() = appLocalRepository.readFlight()

    private fun unloadBoxFromBalanceRemote(
        flightId: String,
        barcode: String,
        isManualInput: Boolean,
        updatedAt: String,
        currentOffice: Int,
    ) = appRemoteRepository.removeBoxFromWarehouseBalance(
        flightId,
        barcode,
        isManualInput,
        updatedAt,
        currentOffice)
        .compose(rxSchedulerFactory.applySingleSchedulers())

    override fun isBoxesUnloaded(): Single<Boolean> {
        return appLocalRepository.dcUnloadedBoxes()
            .map { it == 0 }
            .compose(rxSchedulerFactory.applySingleSchedulers())
    }

    override fun scannerAction(scannerAction: ScannerState) {
        scannerRepository.scannerState(scannerAction)
    }

    override fun switchScreenToClosed(): Completable {
        return screenManager.saveState(FlightStatus.CLOSED)
            .compose(rxSchedulerFactory.applyCompletableSchedulers())
    }

    override fun observeNetworkConnected(): Observable<NetworkState> {
        return networkMonitorRepository.networkConnected()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

}
