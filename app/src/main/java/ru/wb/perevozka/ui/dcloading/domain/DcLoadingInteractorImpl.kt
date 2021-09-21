package ru.wb.perevozka.ui.dcloading.domain

import ru.wb.perevozka.db.AppLocalRepository
import ru.wb.perevozka.db.Optional
import ru.wb.perevozka.db.entity.flighboxes.FlightBoxEntity
import ru.wb.perevozka.db.entity.flighboxes.FlightDstOfficeEntity
import ru.wb.perevozka.db.entity.flighboxes.FlightSrcOfficeEntity
import ru.wb.perevozka.db.entity.flighboxes.ScanProcessStatus
import ru.wb.perevozka.db.entity.flight.FlightEntity
import ru.wb.perevozka.network.api.app.AppRemoteRepository
import ru.wb.perevozka.network.api.app.FlightStatus
import ru.wb.perevozka.network.api.app.entity.warehousescan.WarehouseScanEntity
import ru.wb.perevozka.network.exceptions.BadRequestException
import ru.wb.perevozka.network.monitor.NetworkMonitorRepository
import ru.wb.perevozka.network.monitor.NetworkState
import ru.wb.perevozka.network.rx.RxSchedulerFactory
import ru.wb.perevozka.ui.scanner.domain.ScannerState
import ru.wb.perevozka.ui.scanner.domain.ScannerRepository
import ru.wb.perevozka.utils.managers.ScreenManager
import ru.wb.perevozka.utils.managers.TimeManager
import io.reactivex.*
import io.reactivex.subjects.PublishSubject

class DcLoadingInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val networkMonitorRepository: NetworkMonitorRepository,
    private val appRemoteRepository: AppRemoteRepository,
    private val appLocalRepository: AppLocalRepository,
    private val scannerRepository: ScannerRepository,
    private val timeManager: TimeManager,
    private val screenManager: ScreenManager,
) : DcLoadingInteractor {

    private val actionBarcodeScannedSubject = PublishSubject.create<Pair<String, Boolean>>()

    private val barcodeScannedSubject = PublishSubject.create<String>()

    private val scanLoaderProgressSubject = PublishSubject.create<ScanProgressData>()

    override fun observeNetworkConnected(): Observable<NetworkState> {
        return networkMonitorRepository.networkConnected()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    override fun barcodeManualInput(barcode: String) {
        actionBarcodeScannedSubject.onNext(Pair(barcode, true))
    }

    private fun barcodeScannerInput(): Observable<Pair<String, Boolean>> {
        return scannerRepository.observeBarcodeScanned(barcodeScannedSubject)
            .map { Pair(it, false) }
    }

    override fun observeScanProcess(): Observable<ScanProcessData> {
        return Observable.merge(actionBarcodeScannedSubject, barcodeScannerInput())
            .flatMapSingle {
                Completable.fromAction {
                    scanLoaderProgressSubject.onNext(ScanProgressData.Progress)
                    scannerRepository.scannerState(ScannerState.LoaderProgress)
                }.andThen(Single.just(it))
            }
            .flatMapSingle { boxDefinitionResult(it.first, it.second) }
            .flatMap { warehouseScanOptional(it) }
            .flatMap { boxDefinition ->

                val flight = boxDefinition.flight
                val attachedBoxOptional = boxDefinition.flightBoxOptional
                val warehouseScanOptional = boxDefinition.warehouseScanOptional
                val barcode = boxDefinition.barcode
                val isManual = boxDefinition.isManual
                val updatedAt = boxDefinition.updatedAt
                val codeError = boxDefinition.codeError

                if (attachedBoxOptional is Optional.Success) //коробка уже была добавлена
                    return@flatMap singleGate()
                        .flatMapObservable {
                            Observable.just(ScanBoxData.BoxHasBeenAdded(barcode, it))
                        }
                else return@flatMap when (warehouseScanOptional) {
                    is Optional.Success -> { //информация по коробке получена
                        saveBoxToBalanceByWarehouse( //id dst office найден среди офисов назначения рейса. Добавляем коробку в рейс
                            flight,
                            warehouseScanOptional.data,
                            barcode,
                            updatedAt)
                    }
                    is Optional.Empty -> { //запрос завершился с 400 кодом - определяем код ошибки
                        when (codeError) {
                            ScanProcessStatus.BOX_DOES_NOT_FIT_FLIGHT.name ->
                                Observable.just(ScanBoxData.BoxDoesNotBelongFlight(barcode, ""))
                            ScanProcessStatus.BOX_INFO_DOES_NOT_EXIST.name ->
                                Observable.just(ScanBoxData.BoxDoesNotBelongInfoEmpty(barcode))
                            ScanProcessStatus.BOX_NOT_FROM_THIS_WAREHOUSE.name ->
                                Observable.just(ScanBoxData.BoxDoesNotBelongDc(barcode, ""))
                            else -> Observable.just(ScanBoxData.BoxDoesNotBelongInfoEmpty(barcode))
                        }
                    }
                }
            }
            .flatMap { scanBoxData ->
                appLocalRepository.readAllTakeOnFlightBox()
                    .map { ScanProcessData(scanBoxData, it.size) }
                    .toObservable()
            }
            .flatMap { Completable.fromAction { loaderComplete() }.andThen(Observable.just(it)) }
            .doOnError { loaderComplete() }
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    private fun loaderComplete() {
        scanLoaderProgressSubject.onNext(ScanProgressData.Complete)
        scannerRepository.scannerState(ScannerState.LoaderComplete)
    }

    override fun scanLoaderProgress(): Observable<ScanProgressData> {
        return scanLoaderProgressSubject
    }

    private fun warehouseScanOptional(boxDefinitionResult: BoxDefinitionResult): Observable<BoxDefinitionResult> {
        return with(boxDefinitionResult) {
            warehouseScan(flight.id.toString(), barcode, isManual, updatedAt, flight.dc.id)
                .map { boxDefinitionResult.copy(warehouseScanOptional = Optional.Success(it)) }
                .onErrorReturn {
                    if (it is BadRequestException) {
                        boxDefinitionResult.copy(
                            warehouseScanOptional = Optional.Empty(),
                            codeError = it.error.code)
                    } else throw it
                }
        }
            .toObservable()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    private fun saveBoxToBalanceByWarehouse(
        flightEntity: FlightEntity,
        warehouseScanEntity: WarehouseScanEntity,
        barcode: String,
        updatedAt: String,
    ) = saveBoxToBalanceByInfo(
        barcode = barcode,
        warehouseScanEntity = warehouseScanEntity,
        updatedAt = updatedAt,
        gate = flightEntity.gate,
    ).toObservable()

    private fun WarehouseScanEntity.convertToFlightBoxEntity(barcode: String, updatedAt: String) =
        FlightBoxEntity(
            barcode = barcode,
            updatedAt = updatedAt,
            status = 3,
            onBoard = true,
            srcOffice = convertFlightSrcOfficeEntity(),
            dstOffice = convertFlightDstOfficeEntity(),
        )

    private fun WarehouseScanEntity.convertFlightSrcOfficeEntity() =
        FlightSrcOfficeEntity(
            id = srcOffice.id,
            name = srcOffice.name,
            fullAddress = srcOffice.fullAddress,
            longitude = srcOffice.longitude,
            latitude = srcOffice.latitude,
        )

    private fun WarehouseScanEntity.convertFlightDstOfficeEntity() =
        FlightDstOfficeEntity(
            id = dstOffice.id,
            name = dstOffice.name,
            fullAddress = dstOffice.fullAddress,
            longitude = dstOffice.longitude,
            latitude = dstOffice.latitude,
        )

    private fun saveBoxToBalanceByInfo(
        barcode: String, warehouseScanEntity: WarehouseScanEntity, updatedAt: String, gate: Int,
    ): Single<ScanBoxData> {
        val flightBoxEntity =
            with(warehouseScanEntity) { convertToFlightBoxEntity(barcode, updatedAt) }
        val switchScreen = switchScreen()
        val saveFlightBox = saveFlightBox(flightBoxEntity)
        val boxAdded = boxAdded(barcode, gate.toString())
        return switchScreen
            .andThen(saveFlightBox)
            .andThen(boxAdded)
            .compose(rxSchedulerFactory.applySingleSchedulers())
    }

    override fun removeScannedBoxes(checkedBoxes: List<String>): Completable {
        return flight().flatMapCompletable { removeBoxesFromFlight(it, checkedBoxes) }
            .andThen(appLocalRepository.deleteFlightBoxesByBarcode(checkedBoxes))
            .compose(rxSchedulerFactory.applyCompletableSchedulers())
    }

    private fun removeBoxesFromFlight(flightEntity: FlightEntity, checkedBoxes: List<String>) =
        appRemoteRepository.removeBoxesFromFlight(flightEntity.id.toString(),
            false,
            timeManager.getOffsetLocalTime(),
            flightEntity.dc.id,
            checkedBoxes)

    private fun boxDefinitionResult(
        barcode: String,
        isManual: Boolean,
    ): Single<BoxDefinitionResult> {
        return Single.zip(
            flight(), //рейс
            findAttachedBox(barcode), //коробка уже добавлена
            updatedAt(),
            { flight, findAttachedBox, updatedAt -> //findWarehouseMatchingBox,
                BoxDefinitionResult(flight,
                    findAttachedBox,
                    barcode,
                    isManual,
                    updatedAt)
            }
        ).compose(rxSchedulerFactory.applySingleSchedulers())
    }

    private fun flight() = appLocalRepository.readFlight()

    private fun findAttachedBox(barcode: String) = appLocalRepository.findFlightBox(barcode)

    private fun updatedAt() = Single.just(timeManager.getOffsetLocalTime())

    private fun warehouseScan(
        flightId: String,
        barcode: String,
        isManualInput: Boolean,
        updatedAt: String,
        currentOfficeId: Int,
    ) = appRemoteRepository.warehouseScan(flightId,
        barcode,
        isManualInput,
        updatedAt,
        currentOfficeId)

    private fun saveFlightBox(attachedBoxEntity: FlightBoxEntity) =
        appLocalRepository.saveFlightBox(attachedBoxEntity)

    private fun boxAdded(barcode: String, gate: String) =
        Single.just<ScanBoxData>(ScanBoxData.BoxAdded(barcode, gate))

    override fun observeScannedBoxes(): Observable<List<FlightBoxEntity>> {
        return appLocalRepository.observeTakeOnFlightBoxesByOfficeId().toObservable()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    override fun gate(): Single<String> {
        return singleGate().compose(rxSchedulerFactory.applySingleSchedulers())
    }

    private fun singleGate() = flight().map { it.gate.toString() }

    override fun switchScreen(): Completable {
        return screenManager.saveState(FlightStatus.DCLOADING)
    }

    override fun scannerAction(scannerAction: ScannerState) {
        scannerRepository.scannerState(scannerAction)
    }

}