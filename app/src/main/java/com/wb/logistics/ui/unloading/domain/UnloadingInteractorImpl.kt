package com.wb.logistics.ui.unloading.domain

import com.wb.logistics.db.SuccessOrEmptyData
import com.wb.logistics.db.entity.attachedboxes.AttachedBoxEntity
import com.wb.logistics.db.entity.returnboxes.ReturnBoxEntity
import com.wb.logistics.db.entity.returnboxes.ReturnCurrentOfficeEntity
import com.wb.logistics.db.entity.unloadedboxes.UnloadedBoxEntity
import com.wb.logistics.db.entity.unloadedboxes.UnloadedCurrentOfficeEntity
import com.wb.logistics.network.api.app.AppRepository
import com.wb.logistics.network.rx.RxSchedulerFactory
import com.wb.logistics.ui.scanner.domain.ScannerAction
import com.wb.logistics.ui.scanner.domain.ScannerRepository
import io.reactivex.*
import io.reactivex.subjects.PublishSubject

class UnloadingInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val appRepository: AppRepository,
    private val scannerRepository: ScannerRepository,
) : UnloadingInteractor {

    private val barcodeManualInput = PublishSubject.create<Pair<String, Boolean>>()

    override fun barcodeManualInput(barcode: String) {
        barcodeManualInput.onNext(Pair(barcode, true))
    }

    private fun barcodeScannerInput(): Observable<Pair<String, Boolean>> {
        return scannerRepository.observeBarcodeScanned().map { Pair(it, false) }
    }

    override fun observeScanProcess(dstOfficeId: Int): Observable<UnloadingData> {
        return Observable.merge(barcodeManualInput, barcodeScannerInput())
            .flatMapSingle { boxDefinitionResult(it) }
            .flatMap { boxDefinition ->

                val flight = boxDefinition.flight
                val findUnloadedBox = boxDefinition.findUnloadedBox
                val findReturnBox = boxDefinition.findReturnBox
                val findAttachedBox = boxDefinition.findAttachedBox
                val barcodeScanned = boxDefinition.barcodeScanned
                val isManualInput = boxDefinition.isManualInput
                val updatedAt = appRepository.getOffsetLocalTime()


                val flightId = when (flight) {
                    is SuccessOrEmptyData.Success -> flight.data.id
                    is SuccessOrEmptyData.Empty -> 0
                }
                // TODO: 29.04.2021 добавить конвертер состояния в случае 0 рейса

                when {
                    findUnloadedBox is SuccessOrEmptyData.Success -> //коробка уже выгружена из машины
                        return@flatMap Observable.just(with(findUnloadedBox.data) {
                            UnloadingData.BoxAlreadyUnloaded(barcode)
                        })

                    findReturnBox is SuccessOrEmptyData.Success -> //коробка уже добавлена к возврату
                        return@flatMap Observable.just(with(findReturnBox.data) {
                            UnloadingData.BoxAlreadyReturn(barcode)
                        })

                    findAttachedBox is SuccessOrEmptyData.Success -> { //коробка в списке доставки
                        with(findAttachedBox) {
                            if (dstOfficeId == data.dstOffice.id) { //коробка принадлежит ПВЗ
                                // TODO: 27.04.2021 добавить коробку в базу
                                return@flatMap appRepository.saveUnloadedBox(UnloadedBoxEntity(
                                    flightId,
                                    isManualInput,
                                    barcodeScanned,
                                    updatedAt,
                                    UnloadedCurrentOfficeEntity(dstOfficeId)))

                                    .andThen(removeBoxFromBalance(flightId.toString(), //снятие с баланса
                                        data.barcode,
                                        isManualInput,
                                        updatedAt,
                                        dstOfficeId))
                                    .andThen(appRepository.deleteAttachedBox(data))
                                    .andThen(Observable.just(UnloadingData.BoxUnloadAdded(data.barcode)))
                            } else {
                                return@flatMap Observable.just(UnloadingData.BoxDoesNotBelongPoint(
                                    data.barcode,
                                    data.dstFullAddress))
                            }
                        }
                    }

                    findAttachedBox is SuccessOrEmptyData.Empty -> { //коробки нет в списке доставки - принятие на возврат
                        return@flatMap saveReturnBox(flightId,
                            isManualInput,
                            barcodeScanned,
                            updatedAt,
                            dstOfficeId)
                            .andThen(loadBoxToBalanceRemote(flightId.toString(),
                                barcodeScanned,
                                isManualInput,
                                updatedAt,
                                dstOfficeId))
                            .andThen(Observable.just(UnloadingData.BoxReturnAdded(barcodeScanned)))
                    }
                    else -> return@flatMap Observable.just(UnloadingData.Empty)
                }
            }
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    private fun loadBoxToBalanceRemote(
        flightId: String,
        barcode: String,
        isManualInput: Boolean,
        updatedAt: String,
        currentOffice: Int,
    ) = appRepository.loadBoxToBalanceRemote(
        flightId,
        barcode,
        isManualInput,
        updatedAt,
        currentOffice)
        .onErrorComplete() // TODO: 29.04.2021 реализовать конвертер ошибки
        .compose(rxSchedulerFactory.applyCompletableSchedulers())

    private fun saveReturnBox(
        flightId: Int,
        isManualInput: Boolean,
        barcodeScanned: String,
        updateAt: String,
        dstOfficeId: Int,
    ) = appRepository.saveReturnBox(ReturnBoxEntity(flightId,
        isManualInput,
        barcodeScanned,
        updateAt,
        ReturnCurrentOfficeEntity(dstOfficeId)))

    override fun observeAttachedBoxes(dstOfficeId: Int): Observable<List<AttachedBoxEntity>> {
        return appRepository.observedAttachedBoxes(dstOfficeId)
            .toObservable()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    override fun removeReturnBoxes(checkedBoxes: List<String>): Completable {
        return appRepository.findReturnBoxes(checkedBoxes)
            .flatMapCompletable { returnBoxes ->
                Observable.fromIterable(returnBoxes)
                    .flatMapCompletable {
                        removeReturnBoxRemote(it).andThen(deleteReturnBoxLocal(it))
                    }
            }.compose(rxSchedulerFactory.applyCompletableSchedulers())
    }

    private fun removeReturnBoxRemote(returnBoxEntity: ReturnBoxEntity) =
        with(returnBoxEntity) {
            appRepository.removeBoxFromFlightRemote(
                flightId.toString(),
                barcode,
                isManualInput,
                updatedAt,
                currentOffice.id)
                .onErrorComplete() // TODO: 29.04.2021 реализовать конвертер ошибки
        }

    private fun deleteReturnBoxLocal(returnBoxEntity: ReturnBoxEntity) =
        appRepository.deleteReturnBox(returnBoxEntity).onErrorComplete()

    private fun boxDefinitionResult(param: Pair<String, Boolean>): Single<BoxDefinitionResult> {
        val barcode = param.first
        val isManual = param.second
        return Single.zip(
            flight(), //рейс
            findUnloadedBox(barcode), //коробка есть в списке выгруженных
            findReturnBox(barcode), //коробка есть в списке на возврат
            findAttachedBox(barcode), //коробка есть в списке доставки
            { flight, findUnloadedBox, findReturnBox, findAttachedBox ->
                BoxDefinitionResult(flight,
                    findUnloadedBox,
                    findReturnBox,
                    findAttachedBox,
                    barcode,
                    isManual)
            }
        ).compose(rxSchedulerFactory.applySingleSchedulers())
    }

    private fun flight() = appRepository.readFlight()

    private fun findAttachedBox(barcode: String) = appRepository.findAttachedBox(barcode)

    private fun findUnloadedBox(barcode: String) = appRepository.findUnloadedBox(barcode)

    private fun findReturnBox(barcode: String) = appRepository.findReturnBox(barcode)

    private fun removeBoxFromBalance(
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

    override fun observeUnloadedBoxes(dstOfficeId: Int): Observable<Pair<List<UnloadedBoxEntity>, List<AttachedBoxEntity>>> {
        return Flowable.combineLatest(appRepository.observeUnloadedBoxesByDstOfficeId(dstOfficeId),
            appRepository.observedAttachedBoxes(dstOfficeId),
            { unloaded, attached -> Pair(unloaded, attached) })
            .toObservable()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    override fun observeReturnBoxes(dstOfficeId: Int): Observable<List<ReturnBoxEntity>> {
        return appRepository.observedReturnBoxesByDstOfficeId(dstOfficeId).toObservable()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    override fun scannerAction(scannerAction: ScannerAction) {
        scannerRepository.scannerAction(scannerAction)
    }

    override fun completeUnloading(dstOfficeId: Int): Completable {
        return appRepository.changeFlightOfficeUnloading(dstOfficeId, true, "")
            .compose(rxSchedulerFactory.applyCompletableSchedulers())
    }

}
