package com.wb.logistics.ui.dcunloading.domain

import com.wb.logistics.db.AppLocalRepository
import com.wb.logistics.db.Optional
import com.wb.logistics.db.entity.dcunloadedboxes.*
import com.wb.logistics.db.entity.flighboxes.FlightBoxEntity
import com.wb.logistics.network.api.app.AppRemoteRepository
import com.wb.logistics.network.rx.RxSchedulerFactory
import com.wb.logistics.network.token.TimeManager
import com.wb.logistics.ui.scanner.domain.ScannerAction
import com.wb.logistics.ui.scanner.domain.ScannerRepository
import io.reactivex.*
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

    private fun barcodeScannerInput(): Observable<Pair<String, Boolean>> {
        return scannerRepository.observeBarcodeScanned().map { Pair(it, false) }
    }

    override fun observeScanProcess(): Observable<DcUnloadingData> {
        return Observable.merge(barcodeManualInput, barcodeScannerInput())
            .flatMapSingle { boxDefinitionResult(it.first, it.second) }
            .flatMap { boxDefinition ->

                val flight = boxDefinition.flight
                val findDcUnloadedBox = boxDefinition.findDcUnloadedBox
                val findDcReturnBox = boxDefinition.findDcReturnBox
                val findAttachedBox = boxDefinition.findAttachedBox
                val barcodeScanned = boxDefinition.barcodeScanned
                val isManualInput = boxDefinition.isManualInput
                val updatedAt = timeManager.getOffsetLocalTime()

                val flightId = flight.id

                when {
                    findDcUnloadedBox is Optional.Success -> //коробка уже выгружена из машины
                        return@flatMap Observable.just(with(findDcUnloadedBox.data) {
                            DcUnloadingData.BoxAlreadyUnloaded(barcode)
                        })

                    findDcReturnBox is Optional.Success -> {//коробка найдена в списке на возврат - выгружаем
                        val dstOfficeId = findDcReturnBox.data.dstOffice.id
                        val unloadBoxFromBalanceRemote =
                            unloadBoxFromBalanceRemote(flightId.toString(),
                                barcodeScanned,
                                isManualInput,
                                updatedAt,
                                dstOfficeId)
                        val removeUnloadedReturnBox =
                            appLocalRepository.removeDcUnloadedReturnBox(findDcReturnBox.data)
                        val dcUnloadingBoxAdded =
                            Observable.just(DcUnloadingData.BoxUnloaded(barcodeScanned))

                        return@flatMap unloadBoxFromBalanceRemote.flatMapObservable { unloadedBox ->
                            removeUnloadedReturnBox
                                .andThen(saveUnloadedBox(unloadedBox.copy(updatedAt = updatedAt)))
                                .andThen(dcUnloadingBoxAdded)
                        }
                    }
                    // TODO: 29.06.2021 реализовать
                    findAttachedBox is Optional.Success -> { //коробка в списке доставки и не была выгружена на ПВЗ
                        val dstOfficeId = findAttachedBox.data.dstOffice.id
                        val unloadBoxFromBalanceRemote =
                            unloadBoxFromBalanceRemote(flightId.toString(),
                                barcodeScanned,
                                isManualInput,
                                updatedAt,
                                dstOfficeId)
                        val removeUnloadedReturnBox =
                            appLocalRepository.removeDcUnloadedReturnBox(findAttachedBox.data)
                        val dcUnloadingBoxAdded =
                            Observable.just(DcUnloadingData.BoxUnloaded(barcodeScanned))

                        return@flatMap unloadBoxFromBalanceRemote.flatMapObservable { unloadedBox ->
                            removeUnloadedReturnBox
                                .andThen(saveUnloadedBox(unloadedBox.copy(updatedAt = updatedAt)))
                                .andThen(dcUnloadingBoxAdded)
                        }
                    }
                    // TODO: 05.07.2021 запросить информацию по коробке
                    else -> return@flatMap Observable.just(DcUnloadingData.BoxDoesNotBelongDc)
                }
            }
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    private fun saveUnloadedBox(removedBox: FlightBoxEntity) =
        appLocalRepository.saveDcUnloadedReturnBox(removedBox)

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
        return flight().flatMap { flight ->
            Single.zip(
                findDcUnloadedBox(barcode, flight.dc.id), //коробка есть в списке выгруженных
                findDcReturnBox(barcode, flight.dc.id), //коробка есть в списке на возврат
                findAttachedBox(barcode), //коробка есть в списке доставки
                { findDcUnloadedBox, findDcReturnBox, findAttachedBox ->
                    DcBoxDefinitionResult(flight,
                        findDcUnloadedBox,
                        findDcReturnBox,
                        findAttachedBox,
                        barcode,
                        isManual)
                }
            )
        }.compose(rxSchedulerFactory.applySingleSchedulers())
    }

    private fun flight() = appLocalRepository.readFlight()

    private fun findDcUnloadedBox(barcode: String, currentOfficeId: Int) =
        appLocalRepository.findDcUnloadedBox(barcode, currentOfficeId)

    private fun findDcReturnBox(barcode: String, currentOfficeId: Int) =
        appLocalRepository.findDcReturnBox(barcode, currentOfficeId)

    private fun findAttachedBox(barcode: String) = appLocalRepository.findFlightBox(barcode)

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

    override fun observeDcUnloadedBoxes(): Observable<Pair<DcUnloadingScanBoxEntity, String>> {
        return flight().map { it.dc.id }
            .flatMapObservable { currentOfficeId ->
                Observable.combineLatest(
                    appLocalRepository.observeDcUnloadingScanBox(currentOfficeId).toObservable(),
                    appLocalRepository.observeDcUnloadingBarcodeBox(currentOfficeId).toObservable(),
                    { counter, barcode -> Pair(counter, barcode) }
                )
            }
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    override fun scannerAction(scannerAction: ScannerAction) {
        scannerRepository.scannerAction(scannerAction)
    }

}
