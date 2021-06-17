package com.wb.logistics.ui.dcloading.domain

import com.wb.logistics.db.AppLocalRepository
import com.wb.logistics.db.Optional
import com.wb.logistics.db.entity.attachedboxes.AttachedBoxEntity
import com.wb.logistics.db.entity.attachedboxes.AttachedDstOfficeEntity
import com.wb.logistics.db.entity.attachedboxes.AttachedSrcOfficeEntity
import com.wb.logistics.db.entity.flight.FlightEntity
import com.wb.logistics.db.entity.warehousematchingboxes.WarehouseMatchingBoxEntity
import com.wb.logistics.db.entity.warehousematchingboxes.WarehouseMatchingDstOfficeEntity
import com.wb.logistics.db.entity.warehousematchingboxes.WarehouseMatchingSrcOfficeEntity
import com.wb.logistics.network.api.app.AppRemoteRepository
import com.wb.logistics.network.api.app.FlightStatus
import com.wb.logistics.network.api.app.entity.warehousescan.WarehouseScanEntity
import com.wb.logistics.network.exceptions.BadRequestException
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
        return actionBarcodeScannedSubject
            .flatMapSingle { boxDefinitionResult(it.first, it.second) }
            .flatMap { warehouseScanOptional(it) }
            .flatMap { boxDefinition ->

                val flight = boxDefinition.flight
                val matchingBoxOptional = boxDefinition.matchingBoxOptional
                val attachedBoxOptional = boxDefinition.attachedBoxOptional
                val warehouseScanOptional = boxDefinition.warehouseScanOptional
                val barcode = boxDefinition.barcode
                val isManual = boxDefinition.isManual
                val updatedAt = boxDefinition.updatedAt
                val codeError = boxDefinition.codeError

                when {
                    attachedBoxOptional is Optional.Success -> //коробка уже была добавлена
                        return@flatMap Observable.just(with(attachedBoxOptional.data) {
                            ScanBoxData.BoxHasBeenAdded(barcode, gate.toString())
                        })
                    matchingBoxOptional is Optional.Success -> { //коробка принадлежит рейсу
                        val matchingBox = matchingBoxOptional.data
                        return@flatMap saveBoxToBalanceByMatching(
                            flightId = flight.id,
                            barcode = matchingBox.barcode,
                            isManual = isManual,
                            matchingBox = matchingBox,
                            gate = flight.gate,
                            updatedAt = updatedAt)
                    }
                    matchingBoxOptional is Optional.Empty -> { //коробка не найдена в matching box
                        return@flatMap when (warehouseScanOptional) {
                            is Optional.Success -> { //информация по коробке получена
                                if (warehouseScanOptional.data.srcOffice.id != flight.dc.id) {
                                    Observable.just(ScanBoxData.BoxDoesNotBelongDc(
                                        warehouseScanOptional.data.barcode,
                                        warehouseScanOptional.data.dstOffice.fullAddress))
                                } else {
                                    findFlightOffice(warehouseScanOptional.data.dstOffice.id)
                                        .flatMap { officeOptional ->
                                            when (officeOptional) {
                                                is Optional.Empty ->  //id dst office не найден среди офисов назначения рейса. Не принадлежит рейсу
                                                    Single.just(
                                                        ScanBoxData.BoxDoesNotBelongFlight(
                                                            warehouseScanOptional.data.barcode,
                                                            warehouseScanOptional.data.dstOffice.fullAddress)
                                                    )
                                                is Optional.Success -> saveBoxToBalanceByWarehouse( //id dst office найден среди офисов назначения рейса. Добавляем коробку в рейс
                                                    flight,
                                                    warehouseScanOptional.data,
                                                    barcode,
                                                    isManual,
                                                    updatedAt)
                                            }
                                        }.toObservable()
                                }
                            }
                            is Optional.Empty -> { //запрос завершился с 400 кодом - определяем код ошибки
                                if (codeError == "BOX_DOES_NOT_FIT_FLIGHT") {
                                    Observable.just(ScanBoxData.BoxDoesNotBelongFlight(barcode, ""))
                                } else if (codeError == "BOX_INFO_DOES_NOT_EXIST") {
                                    Observable.just(ScanBoxData.BoxDoesNotBelongInfoEmpty(barcode))
                                } else {
                                    Observable.just(ScanBoxData.BoxDoesNotBelongInfoEmpty(barcode))
                                }
                            }
                        }
                    }
                    else -> return@flatMap Observable.just(ScanBoxData.Empty)
                }
            }.flatMap { scanBoxData ->
                appLocalRepository.readAttachedBoxes()
                    .map { ScanProcessData(scanBoxData, it.size) }
                    .toObservable()
            }.compose(rxSchedulerFactory.applyObservableSchedulers())
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
        isManual: Boolean,
        updatedAt: String,
    ): Single<ScanBoxData> {
        return with(warehouseScanEntity) {
            saveBoxToBalanceByInfo(
                barcode = barcode,
                attachedBoxEntity = convertToAttachedBoxEntity(flightEntity,
                    barcode,
                    isManual,
                    updatedAt),
                gate = flightEntity.gate,
            )
        }
    }

    private fun WarehouseScanEntity.convertToAttachedBoxEntity(
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

    private fun WarehouseScanEntity.convertAttachedDstOfficeEntity() =
        AttachedDstOfficeEntity(
            id = dstOffice.id,
            name = dstOffice.name,
            fullAddress = dstOffice.fullAddress,
            longitude = dstOffice.longitude,
            latitude = dstOffice.latitude,
        )

    private fun WarehouseScanEntity.convertAttachedSrcOfficeEntity() =
        AttachedSrcOfficeEntity(
            id = srcOffice.id,
            name = srcOffice.name,
            fullAddress = srcOffice.fullAddress,
            longitude = srcOffice.longitude,
            latitude = srcOffice.latitude,
        )

    private fun saveBoxToBalanceByInfo(
        barcode: String, attachedBoxEntity: AttachedBoxEntity, gate: Int,
    ): Single<ScanBoxData> {
        val switchScreen = switchScreen()
        val saveBoxScanned = saveAttachedBox(attachedBoxEntity)
        val boxAdded = boxAdded(barcode, gate.toString())
        return switchScreen
            .andThen(saveBoxScanned)
            .andThen(boxAdded)
            .compose(rxSchedulerFactory.applySingleSchedulers())
    }

    private fun saveBoxToBalanceByMatching(
        flightId: Int,
        barcode: String,
        isManual: Boolean,
        matchingBox: WarehouseMatchingBoxEntity,
        gate: Int,
        updatedAt: String,
    ): Observable<ScanBoxData> {
        val switchScreen = switchScreen()
        val saveBoxScanned =
            saveAttachedBox(convertAttachedBox(flightId, matchingBox, gate, isManual, updatedAt))
        val boxAdded = boxAdded(barcode, gate.toString())

        return switchScreen
            .andThen(saveBoxScanned)
            .andThen(deleteMatchingBox(matchingBox))
            .andThen(boxAdded)
            .toObservable()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    private fun convertAttachedBox(
        flightId: Int,
        matchingBoxEntity: WarehouseMatchingBoxEntity,
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
        return flight().flatMapCompletable { removeBoxes(it, checkedBoxes) }
            .andThen(appLocalRepository.findAttachedBoxes(checkedBoxes))
            .flatMap { appLocalRepository.deleteAttachedBoxes(it).andThen(Single.just(it)) }
            .flatMap { convertAttachedBoxesToMatchingBox(it) }
            .flatMapCompletable { appLocalRepository.saveMatchingBoxes(it) }
            .compose(rxSchedulerFactory.applyCompletableSchedulers())
    }

    private fun removeBoxes(flightEntity: FlightEntity, checkedBoxes: List<String>) =
        appRemoteRepository.removeBoxesFromFlight(flightEntity.id.toString(),
            false,
            timeManager.getOffsetLocalTime(),
            flightEntity.dc.id,
            checkedBoxes)

    private fun convertAttachedBoxesToMatchingBox(attachedBoxes: List<AttachedBoxEntity>) =
        Observable.fromIterable(attachedBoxes).map { convertToMatchingBox(it) }.toList()

    private fun convertToMatchingBox(attachedBoxEntity: AttachedBoxEntity) =
        with(attachedBoxEntity) {
            WarehouseMatchingBoxEntity(
                barcode = barcode,
                srcOffice = WarehouseMatchingSrcOfficeEntity(
                    id = srcOffice.id,
                    name = srcOffice.name,
                    fullAddress = srcOffice.fullAddress,
                    longitude = srcOffice.longitude,
                    latitude = srcOffice.latitude,
                ),
                dstOffice = WarehouseMatchingDstOfficeEntity(
                    id = dstOffice.id,
                    name = dstOffice.name,
                    fullAddress = dstOffice.fullAddress,
                    longitude = dstOffice.longitude,
                    latitude = dstOffice.latitude,
                )
            )
        }


    private fun deleteMatchingBox(matchingBoxEntity: WarehouseMatchingBoxEntity) =
        appLocalRepository.deleteMatchingBox(matchingBoxEntity).onErrorComplete()

    private fun deleteAttachedBox(attachedBoxEntity: AttachedBoxEntity) =
        appLocalRepository.deleteAttachedBox(attachedBoxEntity).onErrorComplete()

    private fun saveMatchingBox(matchingBoxEntity: WarehouseMatchingBoxEntity) =
        appLocalRepository.saveMatchingBox(matchingBoxEntity).onErrorComplete()

    private fun boxDefinitionResult(
        barcode: String,
        isManual: Boolean,
    ): Single<BoxDefinitionResult> {
        return Single.zip(
            flight(), //рейс
            findMatchingBox(barcode), //коробка привязана к рейсу
            findAttachedBox(barcode), //коробка уже добавлена
            updatedAt(),
            { flight, findMatchingBox, findAttachedBox, updatedAt ->
                BoxDefinitionResult(flight,
                    findMatchingBox,
                    findAttachedBox,
                    barcode,
                    isManual,
                    updatedAt)
            }
        ).compose(rxSchedulerFactory.applySingleSchedulers())
    }

    private fun flight() = appLocalRepository.readFlight()

    private fun findMatchingBox(barcode: String) = appLocalRepository.findMatchingBox(barcode)

    private fun findAttachedBox(barcode: String) = appLocalRepository.findAttachedBox(barcode)

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

    private fun findFlightOffice(id: Int) = appLocalRepository.findFlightOfficeOptional(id)

    private fun saveAttachedBox(attachedBoxEntity: AttachedBoxEntity) =
        appLocalRepository.saveAttachedBox(attachedBoxEntity)

    private fun boxAdded(barcode: String, gate: String) =
        Single.just<ScanBoxData>(ScanBoxData.BoxAdded(barcode, gate))

    override fun observeScannedBoxes(): Observable<List<AttachedBoxEntity>> {
        return appLocalRepository.observeAttachedBoxes()
            .toObservable()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    override fun switchScreen(): Completable {
        return screenManager.saveState(FlightStatus.DCLOADING)
    }

    override fun scannerAction(scannerAction: ScannerAction) {
        scannerRepository.scannerAction(scannerAction)
    }

}