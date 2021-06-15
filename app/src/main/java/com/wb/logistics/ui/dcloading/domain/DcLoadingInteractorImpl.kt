package com.wb.logistics.ui.dcloading.domain

import com.wb.logistics.db.AppLocalRepository
import com.wb.logistics.db.Optional
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
import com.wb.logistics.network.api.app.entity.boxinfo.BoxInfoEntity
import com.wb.logistics.network.rx.RxSchedulerFactory
import com.wb.logistics.network.token.TimeManager
import com.wb.logistics.ui.scanner.domain.ScannerAction
import com.wb.logistics.ui.scanner.domain.ScannerRepository
import com.wb.logistics.utils.managers.ScreenManager
import io.reactivex.*
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
                val boxMatching = boxDefinition.matchingBox
                val boxAttached = boxDefinition.attachedBox
                val barcode = boxDefinition.barcode
                val isManual = boxDefinition.isManual

                when {
                    boxAttached is Optional.Success -> //коробка уже была отсканирована
                        return@flatMap Observable.just(with(boxAttached.data) {
                            ScanBoxData.BoxHasBeenAdded(barcode, gate.toString())
                        })
                    flight is Optional.Success -> //данные по рейсу актуальны
                        when (boxMatching) {
                            is Optional.Success ->  //коробка принадлежит рейсу
                                return@flatMap saveBoxToBalanceByMatching(
                                    flightId = flight.data.id,
                                    barcode = boxMatching.data.barcode,
                                    isManual = isManual,
                                    officeId = boxMatching.data.srcOffice.id,
                                    matchingBox = boxMatching.data,
                                    gate = flight.data.gate)
                            is Optional.Empty -> { //коробка не найдена в matching box
                                return@flatMap boxDoesNotFindMatching(barcode, flight, isManual)
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

    private fun boxDoesNotFindMatching(
        barcode: String,
        flightOptional: Optional.Success<FlightEntity>,
        isManual: Boolean,
    ): Observable<ScanBoxData> {
        val boxDoesNotBelongInfoEmpty = Single.just(ScanBoxData.BoxDoesNotBelongInfoEmpty(barcode))
        return boxInfo(barcode)
            .flatMap { boxInfoOptional ->
                val box = boxInfoOptional.box
                if (box is Optional.Success)
                    boxInfoSuccess(flightOptional.data, box.data, barcode, isManual)
                else {
                    boxDoesNotBelongInfoEmpty //запрос завершился с 200 кодом, но пустым телом ответа
                }
            }
            .toObservable()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    private fun boxInfoSuccess(
        flightEntity: FlightEntity,
        boxInfoEntity: BoxInfoEntity,
        barcode: String,
        isManual: Boolean,
    ) = findFlightOffice(boxInfoEntity.dstOffice.id)
        .flatMap { officeOptional ->
            when (officeOptional) {
                is Optional.Empty ->
                    Single.just( //id dst office не найден среди офисов назначения рейса. Не принадлежит рейсу
                        ScanBoxData.BoxDoesNotBelongFlight(barcode,
                            boxInfoEntity.srcOffice.fullAddress)
                    )
                is Optional.Success ->
                    saveBoxToBalanceByInfo(flightEntity,
                        boxInfoEntity,
                        barcode,
                        isManual) //id dst office найден среди офисов назначения рейса. Добавляем коробку в рейс
            }
        }

    private fun saveBoxToBalanceByInfo(
        flightEntity: FlightEntity,
        boxInfoEntity: BoxInfoEntity,
        barcode: String,
        isManual: Boolean,
    ): Single<ScanBoxData> {
        val updatedAt = timeManager.getOffsetLocalTime()
        return with(boxInfoEntity) {
            saveBoxToBalanceByInfo(
                flightId = flightEntity.id,
                barcode = barcode,
                isManual = isManual,
                officeId = srcOffice.id,
                attachedBoxEntity = convertAttachedBoxEntity(flightEntity,
                    barcode,
                    isManual,
                    updatedAt),
                gate = flightEntity.gate)
        }
    }

    private fun BoxInfoEntity.convertAttachedBoxEntity(
        flightEntity: FlightEntity,
        barcode: String,
        isManual: Boolean,
        updatedAt: String,
    ) = AttachedBoxEntity(
        flightId = flightEntity.id,
        barcode = barcode,
        gate = flightEntity.gate,
        srcOffice = convertAttachedSrcOfficeEntity(),
        dstOffice = convertAttachedDstOfficeEntity(),
        isManualInput = isManual,
        dstFullAddress = dstOffice.fullAddress,
        updatedAt = updatedAt)

    private fun BoxInfoEntity.convertAttachedDstOfficeEntity() =
        AttachedDstOfficeEntity(
            id = dstOffice.id,
            name = dstOffice.name,
            fullAddress = dstOffice.fullAddress,
            longitude = dstOffice.longitude,
            latitude = dstOffice.latitude,
        )

    private fun BoxInfoEntity.convertAttachedSrcOfficeEntity() =
        AttachedSrcOfficeEntity(
            id = srcOffice.id,
            name = srcOffice.name,
            fullAddress = srcOffice.fullAddress,
            longitude = srcOffice.longitude,
            latitude = srcOffice.latitude,
        )

    private fun saveBoxToBalanceByInfo(
        flightId: Int,
        barcode: String,
        isManual: Boolean,
        officeId: Int,
        attachedBoxEntity: AttachedBoxEntity,
        gate: Int,
    ): Single<ScanBoxData> {
        val updatedAt = timeManager.getOffsetLocalTime()
        val switchScreen = switchScreen()
        val saveBoxScanned = saveAttachedBox(attachedBoxEntity)
        val saveBoxBalanceAwait = boxBalanceAwait(barcode, isManual, officeId, updatedAt)
        val boxAdded = boxAdded(barcode, gate.toString())

        return switchScreen
            .andThen(saveBoxScanned)
            .andThen(saveBoxBalanceAwait)
            .andThen(sendBoxBalanceAwait(flightId.toString()))
            .andThen(boxAdded)
            .compose(rxSchedulerFactory.applySingleSchedulers())
    }

    private fun saveBoxToBalanceByMatching(
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
            findMatchingBox(barcode), //коробка привязана к рейсу
            findAttachedBox(barcode), //коробка уже добавлена
            { flight, findMatchingBox, findAttachedBox ->
                BoxDefinitionResult(flight, findMatchingBox, findAttachedBox, barcode, isManual)
            }
        ).compose(rxSchedulerFactory.applySingleSchedulers())
    }

    private fun flight() = appLocalRepository.readFlight()

    private fun findFlightOffice(id: Int) = appLocalRepository.findFlightOffice(id)

    private fun findAttachedBox(barcode: String) = appLocalRepository.findAttachedBox(barcode)

    private fun findMatchingBox(barcode: String) = appLocalRepository.findMatchingBox(barcode)

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

    private fun boxInfo(barcode: String) = appRemoteRepository.boxInfo(barcode)

    override fun observeScannedBoxes(): Observable<List<AttachedBoxEntity>> {
        return appLocalRepository.observeAttachedBoxes()
            .toObservable()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    override fun sendAwaitBoxesCount(): Single<Int> {
        return flight().flatMap {
            when (it) {
                is Optional.Empty -> Single.error(Throwable())
                is Optional.Success -> Single.just(it.data.id)
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