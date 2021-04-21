package com.wb.logistics.ui.reception.domain

import com.wb.logistics.db.SuccessOrEmptyData
import com.wb.logistics.db.entity.boxtoflight.CurrentOfficeEntity
import com.wb.logistics.db.entity.boxtoflight.ScannedBoxBalanceAwaitEntity
import com.wb.logistics.db.entity.flight.FlightEntity
import com.wb.logistics.db.entity.matchingboxes.MatchingBoxEntity
import com.wb.logistics.db.entity.scannedboxes.DstOfficeEntity
import com.wb.logistics.db.entity.scannedboxes.ScannedBoxEntity
import com.wb.logistics.db.entity.scannedboxes.SrcOfficeEntity
import com.wb.logistics.network.api.app.AppRepository
import com.wb.logistics.network.rx.RxSchedulerFactory
import com.wb.logistics.utils.LogUtils
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject

class ReceptionInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val appRepository: AppRepository,
) : ReceptionInteractor {

    private val actionBarcodeScannedSubject = PublishSubject.create<Pair<String, Boolean>>()

    override fun boxScanned(barcode: String, isManualInput: Boolean) {
        actionBarcodeScannedSubject.onNext(Pair(barcode, isManualInput))
    }

    override fun observeScanProcess(): Observable<ScanBoxData> {
        return actionBarcodeScannedSubject.flatMapSingle { boxDefinitionResult(it) }
            .flatMap { boxDefinition ->

                val flight = boxDefinition.flight
                val matchingBox = boxDefinition.matchingBox
                val flightBoxHasBeenScanned = boxDefinition.flightBoxHasBeenScanned
                val barcodeScanned = boxDefinition.barcodeScanned

                when {
                    flightBoxHasBeenScanned is SuccessOrEmptyData.Success -> //коробка уже была отсканирована
                        return@flatMap Observable.just(with(flightBoxHasBeenScanned.data) {
                            ScanBoxData.BoxHasBeenAdded(barcode, gate.toString())
                        })
                    flight is SuccessOrEmptyData.Success -> //данные по рейсу актуальны
                        when (matchingBox) {
                            is SuccessOrEmptyData.Success ->  //коробка принадлежит рейсу
                                return@flatMap saveBoxToBalance(
                                    flightId = flight.data.id,
                                    barcode = matchingBox.data.barcode,
                                    isManual = boxDefinition.isManual,
                                    officeId = matchingBox.data.srcOffice.id,
                                    matchingBox = matchingBox,
                                    gate = flight.data.gate)
                            is SuccessOrEmptyData.Empty -> { //коробка не принадлежит рейсу
                                return@flatMap notBelongBox(barcodeScanned, flight)
                            }
                        }
                    else -> return@flatMap ObservableSource { ScanBoxData.Empty }
                }
            }
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    private fun notBelongBox(
        barcodeScanned: String,
        flight: SuccessOrEmptyData.Success<FlightEntity>,
    ) = infoBox(barcodeScanned).map { boxInfo ->
        when (boxInfo) {
            is SuccessOrEmptyData.Empty -> {
                ScanBoxData.BoxDoesNotBelongInfo(barcodeScanned)
            }
            is SuccessOrEmptyData.Success -> {
                if (boxInfo.data.box.srcOffice.id == flight.data.dc.id)  //не принадлежит рейсу
                    ScanBoxData.BoxDoesNotBelongFlight(
                        barcodeScanned,
                        boxInfo.data.box.srcOffice.fullAddress,
                        boxInfo.data.flight.gate.toString())
                else ScanBoxData.BoxDoesNotBelongDc( //не принадлежит РЦ
                    barcodeScanned,
                    boxInfo.data.box.srcOffice.fullAddress,
                    boxInfo.data.flight.gate.toString()
                )
            }
        }
    }
        .toObservable()
        .compose(rxSchedulerFactory.applyObservableSchedulers())

    private fun saveBoxToBalance(
        flightId: Int,
        barcode: String,
        isManual: Boolean,
        officeId: Int,
        matchingBox: SuccessOrEmptyData.Success<MatchingBoxEntity>,
        gate: Int,
    ): Observable<ScanBoxData> {
        val saveBoxScanned =
            saveBoxScanned(convertBoxScanned(flightId,
                matchingBox.data,
                gate,
                isManual,
                matchingBox.data.dstOffice.fullAddress))
        val saveBoxBalanceAwait = boxBalanceAwait(barcode, isManual, officeId)
        val boxAdded = boxAdded(barcode, gate.toString())

        return saveBoxScanned
            .andThen(saveBoxBalanceAwait)
            .andThen(sendBoxBalanceAwait(flightId.toString()))
            .andThen(boxAdded)
            .toObservable()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    private fun sendBoxBalanceAwait(flightId: String) =
        appRepository.flightBoxBalanceAwait()
            .flatMapCompletable { boxesBalanceAwait ->
                Observable.fromIterable(boxesBalanceAwait).flatMapCompletable {
                    saveBoxScannedToBalanceRemote(flightId,
                        it.barcode,
                        it.isManualInput,
                        it.dstOffice.id)
                        .andThen(deleteFlightBoxBalanceAwait(it)).onErrorComplete()
                }
            }

    private fun convertBoxScanned(
        flightId: Int,
        matchingBoxEntity: MatchingBoxEntity,
        gate: Int,
        isManual: Boolean,
        dstFullAddress: String,
    ) = with(matchingBoxEntity) {
        LogUtils { logDebugApp(dstFullAddress) }
        ScannedBoxEntity(
            flightId = flightId,
            barcode = barcode,
            gate = gate,
            srcOffice = SrcOfficeEntity(srcOffice.id),
            dstOffice = DstOfficeEntity(dstOffice.id),
            smID = smID,
            isManualInput = isManual,
            dstFullAddress = dstFullAddress)
    }

    override fun deleteScannedBoxes(checkedBoxes: List<String>): Completable {
        return appRepository.loadBoxScanned(checkedBoxes)
            .flatMapCompletable { flightBoxScanned ->
                Observable.fromIterable(flightBoxScanned)
                    .flatMapCompletable {
                        deleteScannedFlightBoxRemote(it).andThen(deleteScannedFlightBoxLocal(it))
                    }
            }.compose(rxSchedulerFactory.applyCompletableSchedulers())
    }

    private fun deleteScannedFlightBoxRemote(flightBoxScannedEntity: ScannedBoxEntity) =
        with(flightBoxScannedEntity) {
            appRepository.deleteFlightBoxScannedRemote(
                flightId.toString(),
                barcode,
                isManualInput,
                srcOffice.id)
        }

    private fun deleteScannedFlightBoxLocal(flightBoxScannedEntity: ScannedBoxEntity) =
        appRepository.deleteBoxScanned(flightBoxScannedEntity).onErrorComplete()

    private fun deleteFlightBoxBalanceAwait(flightBoxBalanceAwaitEntity: ScannedBoxBalanceAwaitEntity) =
        appRepository.deleteFlightBoxBalanceAwait(flightBoxBalanceAwaitEntity).onErrorComplete()

    private fun boxDefinitionResult(param: Pair<String, Boolean>): Single<BoxDefinitionResult> {
        val barcodeScanned = param.first
        val isManual = param.second
        return Single.zip(
            flight(), //рейс
            findFlightBox(barcodeScanned), //коробка привязана к рейсу
            findFlightBoxScanned(barcodeScanned), //коробка уже добавлена
            { flight, findFlightBoxScanned, findFlightBox ->
                BoxDefinitionResult(flight,
                    findFlightBox,
                    findFlightBoxScanned,
                    barcodeScanned,
                    isManual)
            }
        ).compose(rxSchedulerFactory.applySingleSchedulers())
    }

    private fun flight() = appRepository.readFlight()

    private fun findFlightBoxScanned(barcode: String) = appRepository.findBoxScanned(barcode)

    private fun findFlightBox(barcode: String) = appRepository.findMatchingBox(barcode)

    private fun saveBoxScannedToBalanceRemote(
        flightId: String,
        barcode: String,
        isManualInput: Boolean,
        currentOffice: Int,
    ) = appRepository.flightBoxScannedToBalanceRemote(
        flightId,
        barcode,
        isManualInput,
        currentOffice)

    private fun boxBalanceAwait(
        barcode: String,
        isManualInput: Boolean,
        currentOffice: Int,
    ) = appRepository.saveFlightBoxBalanceAwait(
        ScannedBoxBalanceAwaitEntity(barcode, isManualInput, CurrentOfficeEntity(currentOffice)))

    private fun saveBoxScanned(flightBoxScanned: ScannedBoxEntity) =
        appRepository.saveBoxScanned(flightBoxScanned)

    private fun boxAdded(barcode: String, gate: String) =
        Single.just<ScanBoxData>(ScanBoxData.BoxAdded(barcode, gate))

    private fun infoBox(barcode: String) = appRepository.boxInfo(barcode)

    override fun observeScannedBoxes(): Observable<List<ScannedBoxEntity>> {
        return appRepository.observeBoxesScanned().toObservable()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    override fun readBoxesScanned(): Single<List<ScannedBoxEntity>> {
        return appRepository.readBoxesScanned().compose(rxSchedulerFactory.applySingleSchedulers())
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

    override fun addMockScannedBox(): Completable {
        return Observable.fromIterable(listOf(1,
            2,
            3,
            4,
            5,
            6,
            7,
            8,
            9,
            10,
            11,
            12,
            13,
            14,
            15,
            16,
            17,
            18,
            19,
            20))
            .map {
                val barcode = "TRBX12345678910$it"
                if (it % 2 == 0) {
                    createMockScannedBoxEntity(barcode,
                        10,
                        "ПВЗ Москва, ул. Карамазова, 10")
                } else if (it % 3 == 0) {
                    createMockScannedBoxEntity(barcode,
                        10,
                        "ПВЗ Москва, ул. Карамазова, 11")
                } else if (it % 5 == 0) {
                    createMockScannedBoxEntity(barcode,
                        10,
                        "ПВЗ Москва, ул. Карамазова, 12")

                } else {
                    createMockScannedBoxEntity(barcode,
                        11,
                        "ПВЗ Москва, ул. Карамазова, 13")
                }
            }
            .flatMapCompletable { saveBoxScanned(it) }
    }

    private fun createMockScannedBoxEntity(
        barcode: String,
        gate: Int,
        dstFullAddress: String,
    ): ScannedBoxEntity {
        return ScannedBoxEntity(
            flightId = 123,
            barcode = barcode,
            gate = gate,
            srcOffice = SrcOfficeEntity(10),
            dstOffice = DstOfficeEntity(20),
            smID = 10,
            isManualInput = false,
            dstFullAddress = dstFullAddress)
    }

}