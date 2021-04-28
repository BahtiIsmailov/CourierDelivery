package com.wb.logistics.ui.unloading.domain

import com.wb.logistics.db.SuccessOrEmptyData
import com.wb.logistics.db.entity.attachedboxes.AttachedBoxEntity
import com.wb.logistics.db.entity.attachedboxesawait.AttachedBoxBalanceAwaitEntity
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
                val isManual = boxDefinition.isManual


                val flightId = when (flight) {
                    is SuccessOrEmptyData.Success -> flight.data.id
                    is SuccessOrEmptyData.Empty -> 0
                }

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
                                val updatedAt = appRepository.getOffsetLocalTime()
                                return@flatMap appRepository.saveUnloadedBox(UnloadedBoxEntity(
                                    flightId,
                                    isManual,
                                    barcodeScanned,
                                    updatedAt,
                                    UnloadedCurrentOfficeEntity(dstOfficeId)))

                                    .andThen(saveBoxScannedToBalanceRemote(flightId.toString(), //сохранение на сервере
                                        data.barcode,
                                        isManual,
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
                        val updatedAt = appRepository.getOffsetLocalTime()
                        return@flatMap saveReturnBox(flightId,
                            isManual,
                            barcodeScanned,
                            updatedAt,
                            dstOfficeId)
                            .andThen(Observable.just(UnloadingData.BoxReturnAdded(barcodeScanned)))
                    }
                    else -> return@flatMap Observable.just(UnloadingData.Empty)
                }
            }
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    private fun saveReturnBox(
        flightId: Int,
        isManual: Boolean,
        barcodeScanned: String,
        updateAt: String,
        dstOfficeId: Int,
    ) = appRepository.saveReturnBox(ReturnBoxEntity(flightId,
        isManual,
        barcodeScanned,
        updateAt,
        ReturnCurrentOfficeEntity(dstOfficeId)))

    override fun observeAttachedBoxesByDstOfficeId(dstOfficeId: Int): Observable<List<AttachedBoxEntity>> {
        return appRepository.observedAttachedBoxesByDstOfficeId(dstOfficeId)
            .toObservable()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    private fun sendBoxBalanceAwait(flightId: String) =
        appRepository.flightBoxBalanceAwait()
            .flatMapCompletable { boxesBalanceAwait ->
                val updatedAt = appRepository.getOffsetLocalTime()
                Observable.fromIterable(boxesBalanceAwait).flatMapCompletable {
                    saveBoxScannedToBalanceRemote(flightId,
                        it.barcode,
                        it.isManualInput,
                        updatedAt,
                        it.dstOffice.id)
                        .andThen(deleteFlightBoxBalanceAwait(it)).onErrorComplete()
                }
            }

    override fun deleteScannedBoxes(checkedBoxes: List<String>): Completable {
        return appRepository.loadAttachedBoxes(checkedBoxes)
            .flatMapCompletable { flightBoxScanned ->
                Observable.fromIterable(flightBoxScanned)
                    .flatMapCompletable {
                        deleteScannedFlightBoxRemote(it).andThen(deleteScannedFlightBoxLocal(it))
                    }
            }.compose(rxSchedulerFactory.applyCompletableSchedulers())
    }

    private fun deleteScannedFlightBoxRemote(flightBoxScannedEntity: AttachedBoxEntity) =
        with(flightBoxScannedEntity) {
            appRepository.deleteFlightBoxScannedRemote(
                flightId.toString(),
                barcode,
                isManualInput,
                updatedAt,
                srcOffice.id)
        }

    private fun deleteScannedFlightBoxLocal(flightBoxScannedEntity: AttachedBoxEntity) =
        appRepository.deleteAttachedBox(flightBoxScannedEntity).onErrorComplete()

    private fun deleteFlightBoxBalanceAwait(flightBoxBalanceAwaitEntity: AttachedBoxBalanceAwaitEntity) =
        appRepository.deleteFlightBoxBalanceAwait(flightBoxBalanceAwaitEntity).onErrorComplete()

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

    private fun saveBoxScannedToBalanceRemote(
        flightId: String,
        barcode: String,
        isManualInput: Boolean,
        updatedAt: String,
        currentOffice: Int,
    ) = appRepository.saveBoxScannedToBalanceRemote(
        flightId,
        barcode,
        isManualInput,
        updatedAt,
        currentOffice)
        .onErrorComplete()
        .compose(rxSchedulerFactory.applyCompletableSchedulers())

//    private fun saveBoxScanned(flightBoxScanned: AttachedBoxEntity) =
//        appRepository.saveAttachedBox(flightBoxScanned)

    override fun observeUnloadedBoxes(dstOfficeId: Int): Observable<Pair<List<UnloadedBoxEntity>, List<AttachedBoxEntity>>> {
        return Flowable.combineLatest(appRepository.observeUnloadedBoxesByDstOfficeId(dstOfficeId),
            appRepository.observedAttachedBoxesByDstOfficeId(dstOfficeId),
            { unloaded, attached -> Pair(unloaded, attached) })
            .toObservable()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    override fun observeReturnBoxes(dstOfficeId: Int): Observable<List<ReturnBoxEntity>> {
        return appRepository.observedReturnBoxesByDstOfficeId(dstOfficeId).toObservable()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    override fun readBoxesScanned(): Single<List<AttachedBoxEntity>> {
        return appRepository.readAttached().compose(rxSchedulerFactory.applySingleSchedulers())
    }

    override fun sendAwaitBoxes(): Single<Int> {
        return flight().flatMap {
            when (it) {
                is SuccessOrEmptyData.Empty -> Single.error(Throwable())
                is SuccessOrEmptyData.Success -> Single.just(it.data.id)
            }
        }
            .map { it.toString() }
            .flatMapCompletable { sendBoxBalanceAwait(it) }
            .andThen(appRepository.flightBoxBalanceAwait().map { it.size })
    }

    override fun scannerAction(scannerAction: ScannerAction) {
        scannerRepository.scannerAction(scannerAction)
    }

}
