package com.wb.logistics.ui.dcloading.domain

import com.wb.logistics.db.AppLocalRepository
import com.wb.logistics.db.SuccessOrEmptyData
import com.wb.logistics.db.entity.attachedboxes.AttachedBoxEntity
import com.wb.logistics.db.entity.attachedboxes.AttachedDstOfficeEntity
import com.wb.logistics.db.entity.attachedboxes.AttachedSrcOfficeEntity
import com.wb.logistics.db.entity.attachedboxesawait.AttachedBoxBalanceAwaitEntity
import com.wb.logistics.db.entity.attachedboxesawait.AttachedBoxCurrentOfficeEntity
import com.wb.logistics.db.entity.flight.FlightEntity
import com.wb.logistics.db.entity.matchingboxes.MatchingBoxEntity
import com.wb.logistics.db.entity.matchingboxes.MatchingDstOfficeEntity
import com.wb.logistics.db.entity.matchingboxes.MatchingSrcOfficeEntity
import com.wb.logistics.network.api.app.AppRemoteRepository
import com.wb.logistics.network.api.app.FlightStatus
import com.wb.logistics.network.rx.RxSchedulerFactory
import com.wb.logistics.network.token.TimeManager
import com.wb.logistics.ui.scanner.domain.ScannerAction
import com.wb.logistics.ui.scanner.domain.ScannerRepository
import com.wb.logistics.utils.managers.ScreenManager
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject

class DcLoadingInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val appRemoteRepository: AppRemoteRepository,
    private val appLocalRepository: AppLocalRepository,
    private val scannerRepository: ScannerRepository,
    private val timeManager: TimeManager,
    private val screenManager: ScreenManager,
) : DcLoadingInteractor {

    private val actionBarcodeScannedSubject = PublishSubject.create<Pair<String, Boolean>>()

    override fun boxScanned(barcode: String, isManualInput: Boolean) {
        actionBarcodeScannedSubject.onNext(Pair(barcode, isManualInput))
    }

    override fun observeScanProcess(): Observable<ScanProcessData> {
        return actionBarcodeScannedSubject.flatMapSingle {
            boxDefinitionResult(it.first, it.second)
        }
            .flatMap { boxDefinition ->

                val flight = boxDefinition.flight
                val matchingBox = boxDefinition.matchingBox
                val flightBoxHasBeenScanned = boxDefinition.flightBoxHasBeenScanned
                val barcodeScanned = boxDefinition.barcode

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
                                    matchingBox = matchingBox.data,
                                    gate = flight.data.gate)
                            is SuccessOrEmptyData.Empty -> { //коробка не принадлежит рейсу
                                return@flatMap notBelongBox(barcodeScanned, flight)
                            }
                        }
                    else -> return@flatMap ObservableSource { ScanBoxData.Empty }
                }
            }.flatMap { scanBoxData ->
                appLocalRepository.readAttachedBoxes()
                    .map { ScanProcessData(scanBoxData, it.size) }
                    .toObservable()
            }.compose(rxSchedulerFactory.applyObservableSchedulers())
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
        matchingBox: MatchingBoxEntity,
        gate: Int,
    ): Observable<ScanBoxData> {
        val updatedAt = timeManager.getOffsetLocalTime()
        val switchScreen = switchScreen()
        val saveBoxScanned =
            saveAttachedBox(convertAttachedBox(flightId,
                matchingBox,
                gate,
                isManual,
                updatedAt))
        val saveBoxBalanceAwait = boxBalanceAwait(barcode, isManual, officeId, updatedAt)
        val boxAdded = boxAdded(barcode, gate.toString())

        return switchScreen
            .andThen(saveBoxScanned)
            .andThen(saveBoxBalanceAwait)
            .andThen(sendBoxBalanceAwait(flightId.toString()))
            .andThen(deleteMatchingBox(matchingBox))
            .andThen(boxAdded)
            .toObservable()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    private fun sendBoxBalanceAwait(flightId: String) =
        appLocalRepository.attachedBoxesBalanceAwait()
            .flatMapCompletable { boxesBalanceAwait ->
                Observable.fromIterable(boxesBalanceAwait)
                    .flatMapCompletable {
                        warehouseBoxToBalanceRemote(flightId,
                            it.barcode,
                            it.isManualInput,
                            it.updatedAt,
                            it.dstOffice.id)
                            .andThen(deleteFlightBoxBalanceAwait(it)).onErrorComplete()
                    }
            }

    private fun convertAttachedBox(
        flightId: Int,
        matchingBoxEntity: MatchingBoxEntity,
        gate: Int,
        isManual: Boolean,
        updatedAt: String,
    ) = with(matchingBoxEntity) {
        AttachedBoxEntity(
            flightId = flightId,
            barcode = barcode,
            gate = gate,
            srcOffice = AttachedSrcOfficeEntity(
                id = srcOffice.id,
                name = srcOffice.name,
                fullAddress = srcOffice.fullAddress,
                longitude = srcOffice.longitude,
                latitude = srcOffice.latitude,
            ),
            dstOffice = AttachedDstOfficeEntity(
                id = dstOffice.id,
                name = dstOffice.name,
                fullAddress = dstOffice.fullAddress,
                longitude = dstOffice.longitude,
                latitude = dstOffice.latitude,
            ),
            isManualInput = isManual,
            dstFullAddress = dstOffice.fullAddress,
            updatedAt = updatedAt)
    }

    override fun deleteScannedBoxes(checkedBoxes: List<String>): Completable {
        return appLocalRepository.findAttachedBoxes(checkedBoxes)
            .flatMapCompletable { flightBoxScanned ->
                Observable.fromIterable(flightBoxScanned)
                    .flatMapCompletable {
                        deleteAttachedBoxRemote(it)
                            .andThen(deleteAttachedBox(it))
                            .andThen(saveMatchingBox(convertToMatchingBox(it)))
                    }
            }.compose(rxSchedulerFactory.applyCompletableSchedulers())
    }

    private fun convertToMatchingBox(attachedBoxEntity: AttachedBoxEntity) =
        with(attachedBoxEntity) {
            MatchingBoxEntity(
                barcode = barcode,
                srcOffice = MatchingSrcOfficeEntity(
                    id = srcOffice.id,
                    name = srcOffice.name,
                    fullAddress = srcOffice.fullAddress,
                    longitude = srcOffice.longitude,
                    latitude = srcOffice.latitude,
                ),
                dstOffice = MatchingDstOfficeEntity(
                    id = dstOffice.id,
                    name = dstOffice.name,
                    fullAddress = dstOffice.fullAddress,
                    longitude = dstOffice.longitude,
                    latitude = dstOffice.latitude,
                )
            )
        }

    private fun deleteAttachedBoxRemote(attachedBoxEntity: AttachedBoxEntity) =
        with(attachedBoxEntity) {
            appRemoteRepository.removeBoxFromFlight(
                flightId.toString(),
                barcode,
                isManualInput,
                updatedAt,
                srcOffice.id)
        }


    private fun deleteMatchingBox(matchingBoxEntity: MatchingBoxEntity) =
        appLocalRepository.deleteMatchingBox(matchingBoxEntity).onErrorComplete()

    private fun deleteAttachedBox(attachedBoxEntity: AttachedBoxEntity) =
        appLocalRepository.deleteAttachedBox(attachedBoxEntity).onErrorComplete()

    private fun saveMatchingBox(matchingBoxEntity: MatchingBoxEntity) =
        appLocalRepository.saveMatchingBox(matchingBoxEntity).onErrorComplete()

    private fun deleteFlightBoxBalanceAwait(flightBoxBalanceAwaitEntity: AttachedBoxBalanceAwaitEntity) =
        appLocalRepository.deleteAttachedBoxBalanceAwait(flightBoxBalanceAwaitEntity)
            .onErrorComplete()

    private fun boxDefinitionResult(
        barcode: String,
        isManual: Boolean,
    ): Single<BoxDefinitionResult> {
        return Single.zip(
            flight(), //рейс
            findFlightBox(barcode), //коробка привязана к рейсу
            findFlightBoxScanned(barcode), //коробка уже добавлена
            { flight, findFlightBoxScanned, findFlightBox ->
                BoxDefinitionResult(flight,
                    findFlightBox,
                    findFlightBoxScanned,
                    barcode,
                    isManual)
            }
        ).compose(rxSchedulerFactory.applySingleSchedulers())
    }

    private fun flight() = appLocalRepository.readFlight()

    private fun findFlightBoxScanned(barcode: String) = appLocalRepository.findAttachedBox(barcode)

    private fun findFlightBox(barcode: String) = appLocalRepository.findMatchingBox(barcode)

    private fun warehouseBoxToBalanceRemote(
        flightId: String,
        barcode: String,
        isManualInput: Boolean,
        updatedAt: String,
        currentOffice: Int,
    ) = appRemoteRepository.warehouseBoxToBalance(
        flightId,
        barcode,
        isManualInput,
        updatedAt,
        currentOffice)

    private fun boxBalanceAwait(
        barcode: String,
        isManualInput: Boolean,
        currentOffice: Int,
        updatedAt: String,
    ) = appLocalRepository.saveAttachedBoxBalanceAwait(
        AttachedBoxBalanceAwaitEntity(barcode,
            isManualInput,
            AttachedBoxCurrentOfficeEntity(currentOffice),
            updatedAt))

    private fun saveAttachedBox(attachedBoxEntity: AttachedBoxEntity) =
        appLocalRepository.saveAttachedBox(attachedBoxEntity)

    private fun boxAdded(barcode: String, gate: String) =
        Single.just<ScanBoxData>(ScanBoxData.BoxAdded(barcode, gate))

    private fun infoBox(barcode: String) = appRemoteRepository.boxInfo(barcode)

    override fun observeScannedBoxes(): Observable<List<AttachedBoxEntity>> {
        return appLocalRepository.observeAttachedBoxes()
            .toObservable()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    override fun sendAwaitBoxesCount(): Single<Int> {
        return flight().flatMap {
            when (it) {
                is SuccessOrEmptyData.Empty -> Single.error(Throwable())
                is SuccessOrEmptyData.Success -> Single.just(it.data.id)
            }
        }
            .map { it.toString() }
            .flatMapCompletable { sendBoxBalanceAwait(it) }
            .andThen(appLocalRepository.attachedBoxesBalanceAwait().map { it.size })
    }

    override fun switchScreen(): Completable {
        return screenManager.saveState(FlightStatus.DCLOADING)
    }

    override fun scannerAction(scannerAction: ScannerAction) {
        scannerRepository.scannerAction(scannerAction)
    }

}