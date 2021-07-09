package com.wb.logistics.ui.dcunloading.domain

import com.wb.logistics.db.AppLocalRepository
import com.wb.logistics.db.Optional
import com.wb.logistics.db.entity.dcunloadedboxes.DcReturnHandleBarcodeEntity
import com.wb.logistics.db.entity.dcunloadedboxes.DcUnloadingBarcodeEntity
import com.wb.logistics.db.entity.flighboxes.FlightBoxEntity
import com.wb.logistics.network.api.app.AppRemoteRepository
import com.wb.logistics.network.exceptions.BadRequestException
import com.wb.logistics.network.rx.RxSchedulerFactory
import com.wb.logistics.network.token.TimeManager
import com.wb.logistics.ui.scanner.domain.ScannerAction
import com.wb.logistics.ui.scanner.domain.ScannerRepository
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject

class DcUnloadingInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val appRemoteRepository: AppRemoteRepository,
    private val appLocalRepository: AppLocalRepository,
    private val scannerRepository: ScannerRepository,
    private val timeManager: TimeManager,
) : DcUnloadingInteractor {

    private val barcodeManualInput = PublishSubject.create<Pair<String, Boolean>>()

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

    private fun barcodeScannerInput(): Observable<Pair<String, Boolean>> {
        return scannerRepository.observeBarcodeScanned().map { Pair(it, false) }
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
        val boxUnloaded = Single.just(DcUnloadingAction.BoxUnloaded(barcode))
        return saveFlightBox
            .andThen(boxUnloaded)
            .toObservable()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    private fun saveFlightBox(attachedBoxEntity: FlightBoxEntity) =
        appLocalRepository.saveFlightBox(attachedBoxEntity)


    private fun observeScanProcess(): Observable<DcUnloadingAction> {
        return Observable.merge(barcodeManualInput, barcodeScannerInput())
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
                        if (codeError == "BOX_DOES_NOT_FIT_FLIGHT") {
                            Observable.just(DcUnloadingAction.BoxDoesNotBelongFlight)
                        } else if (codeError == "BOX_INFO_DOES_NOT_EXIST") {
                            Observable.just(DcUnloadingAction.BoxDoesNotBelongInfoEmpty(barcode))
                        } else if (codeError == "BOX_NOT_FROM_THIS_WAREHOUSE") {
                            Observable.just(DcUnloadingAction.BoxDoesNotBelongDc)
                        } else {
                            Observable.just(DcUnloadingAction.BoxDoesNotBelongInfoEmpty(barcode))
                        }
                    }

                }
            }.startWith(Observable.just(DcUnloadingAction.Init))
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

    override fun scannerAction(scannerAction: ScannerAction) {
        scannerRepository.scannerAction(scannerAction)
    }

}
