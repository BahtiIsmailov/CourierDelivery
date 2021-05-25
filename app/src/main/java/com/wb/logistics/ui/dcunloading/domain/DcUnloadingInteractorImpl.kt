package com.wb.logistics.ui.dcunloading.domain

import com.wb.logistics.db.SuccessOrEmptyData
import com.wb.logistics.db.entity.dcunloadedboxes.*
import com.wb.logistics.network.api.app.AppRepository
import com.wb.logistics.network.rx.RxSchedulerFactory
import com.wb.logistics.ui.scanner.domain.ScannerAction
import com.wb.logistics.ui.scanner.domain.ScannerRepository
import io.reactivex.*
import io.reactivex.subjects.PublishSubject

class DcUnloadingInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val appRepository: AppRepository,
    private val scannerRepository: ScannerRepository,
) : DcUnloadingInteractor {

    private var barcodeScanned = ""

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
                val findReturnBox = boxDefinition.findReturnBox
                val findAttachedBox = boxDefinition.findAttachedBox
                barcodeScanned = boxDefinition.barcodeScanned
                val isManualInput = boxDefinition.isManualInput
                val updatedAt = appRepository.getOffsetLocalTime()

                val flightId = when (flight) {
                    is SuccessOrEmptyData.Success -> flight.data.id
                    is SuccessOrEmptyData.Empty -> 0
                }
                // TODO: 29.04.2021 добавить конвертер состояния в случае 0 рейса

                when {
                    findDcUnloadedBox is SuccessOrEmptyData.Success -> //коробка уже выгружена из машины
                        return@flatMap Observable.just(with(findDcUnloadedBox.data) {
                            DcUnloadingData.BoxAlreadyUnloaded(barcode)
                        })

                    findReturnBox is SuccessOrEmptyData.Success -> {//коробка найдена в списке возврата
                        val attachAt = appRepository.getOffsetLocalTime()
                        val dstOfficeId = findReturnBox.data.currentOffice.id
                        val dcUnloadedReturnBoxEntity = DcUnloadedReturnBoxEntity(
                            flightId,
                            isManualInput,
                            barcodeScanned,
                            updatedAt,
                            attachAt,
                            DcUnloadedCurrentOfficeEntity(dstOfficeId))
                        val saveDcUnloadedReturnBox =
                            appRepository.saveDcUnloadedReturnBox(dcUnloadedReturnBoxEntity)
                        val removeBoxFromBalanceRemote =
                            removeBoxFromBalanceRemote(flightId.toString(),
                                barcodeScanned,
                                isManualInput,
                                updatedAt,
                                dstOfficeId)
                        val deleteReturnBox = appRepository.deleteReturnBox(findReturnBox.data)
                        val dcUnloadingAdded =
                            Observable.just(DcUnloadingData.BoxUnload(barcodeScanned))

                        return@flatMap saveDcUnloadedReturnBox
                            .andThen(removeBoxFromBalanceRemote)
                            .andThen(deleteReturnBox)
                            .andThen(dcUnloadingAdded)
                    }

                    findAttachedBox is SuccessOrEmptyData.Success -> { //коробка в списке доставки и не была выгружена на ПВЗ
                        val attachAt = appRepository.getOffsetLocalTime()
                        val dstOfficeId = findAttachedBox.data.dstOffice.id
                        val dcUnloadedBoxEntity = DcUnloadedBoxEntity(
                            flightId,
                            isManualInput,
                            barcodeScanned,
                            updatedAt,
                            attachAt,
                            DcUnloadedCurrentOfficeEntity(dstOfficeId))
                        val saveDcUnloadedBox = appRepository.saveDcUnloadedBox(dcUnloadedBoxEntity)
                        val removeBoxFromBalanceRemote =
                            removeBoxFromBalanceRemote(flightId.toString(),
                                barcodeScanned,
                                isManualInput,
                                updatedAt,
                                dstOfficeId)
                        val deleteReturnBox = appRepository.deleteAttachedBox(findAttachedBox.data)
                        val dcUnloadingAdded =
                            Observable.just(DcUnloadingData.BoxUnload(barcodeScanned))

                        return@flatMap saveDcUnloadedBox
                            .andThen(removeBoxFromBalanceRemote)
                            .andThen(deleteReturnBox)
                            .andThen(dcUnloadingAdded)
                    }
                    else -> return@flatMap Observable.just(DcUnloadingData.BoxDoesNotBelongDc)
                }
            }
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }
    override fun findDcUnloadedHandleBoxes(): Single<List<DcUnloadingHandleBoxEntity>> {
        return appRepository.findDcUnloadedHandleBoxes()
            .compose(rxSchedulerFactory.applySingleSchedulers())
    }

    override fun findDcUnloadedListBoxes(): Single<List<DcUnloadingListBoxEntity>> {
        return appRepository.findDcUnloadedListBoxes()
            .compose(rxSchedulerFactory.applySingleSchedulers())
    }

    private fun boxDefinitionResult(
        barcode: String,
        isManual: Boolean,
    ): Single<DcBoxDefinitionResult> {
        return Single.zip(
            flight(), //рейс
            findDcUnloadedBox(barcode), //коробка есть в списке выгруженных
            findReturnBox(barcode), //коробка есть в списке на возврат
            findAttachedBox(barcode), //коробка есть в списке доставки
            { flight, findDcUnloadedBox, findReturnBox, findAttachedBox ->
                DcBoxDefinitionResult(flight,
                    findDcUnloadedBox,
                    findReturnBox,
                    findAttachedBox,
                    barcode,
                    isManual)
            }
        ).compose(rxSchedulerFactory.applySingleSchedulers())
    }

    private fun flight() = appRepository.readFlight()

    private fun findDcUnloadedBox(barcode: String) = appRepository.findDcUnloadedBox(barcode)

    private fun findReturnBox(barcode: String) = appRepository.findReturnBox(barcode)

    private fun findAttachedBox(barcode: String) = appRepository.findAttachedBox(barcode)

    private fun removeBoxFromBalanceRemote(
        flightId: String,
        barcode: String,
        isManualInput: Boolean,
        updatedAt: String,
        currentOffice: Int,
    ) = appRepository.removeBoxFromBalanceRemote(
        flightId,
        barcode,
        isManualInput,
        updatedAt,
        currentOffice)
        .onErrorComplete() // TODO: 29.04.2021 реализовать конвертер ошибки
        .compose(rxSchedulerFactory.applyCompletableSchedulers())

    override fun observeDcUnloadedBoxes(): Observable<DcUnloadingScanBoxEntity> {
        return appRepository.observeDcUnloadingScanBox()
            .toObservable()
            .map { it.copy(barcode = barcodeScanned) }
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    override fun scannerAction(scannerAction: ScannerAction) {
        scannerRepository.scannerAction(scannerAction)
    }

}
