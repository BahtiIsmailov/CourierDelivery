package com.wb.logistics.ui.unloading.domain

import com.wb.logistics.db.AppLocalRepository
import com.wb.logistics.db.Optional
import com.wb.logistics.db.entity.attachedboxes.AttachedBoxEntity
import com.wb.logistics.db.entity.flighboxes.*
import com.wb.logistics.db.entity.flight.FlightEntity
import com.wb.logistics.db.entity.pvzmatchingboxes.PvzMatchingBoxEntity
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

class UnloadingInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val appRemoteRepository: AppRemoteRepository,
    private val appLocalRepository: AppLocalRepository,
    private val scannerRepository: ScannerRepository,
    private val timeManager: TimeManager,
    private val screenManager: ScreenManager,
) : UnloadingInteractor {

    private val barcodeManualInput = PublishSubject.create<Pair<String, Boolean>>()

    override fun barcodeManualInput(barcode: String) {
        barcodeManualInput.onNext(Pair(barcode, true))
    }

    private fun barcodeScannerInput(): Observable<Pair<String, Boolean>> {
        return scannerRepository.observeBarcodeScanned().map { Pair(it, false) }
    }

    override fun observeScanProcess(currentOfficeId: Int): Observable<UnloadingData> {
        return Observable.merge(barcodeManualInput, barcodeScannerInput())
            .flatMapSingle { boxDefinitionResult(it.first, it.second, currentOfficeId) }
            .flatMap { boxDefinition ->

                val flight = boxDefinition.flight
                val findUnloadedBox = boxDefinition.findUnloadedBox
                val findReturnBox = boxDefinition.findReturnBox
                val findAttachedBox = boxDefinition.findAttachedBox
                val findPvzMatchingBox = boxDefinition.findPvzMatchingBox
                val barcodeScanned = boxDefinition.barcodeScanned
                val isManualInput = boxDefinition.isManualInput
                val updatedAt = timeManager.getOffsetLocalTime()
                val flightId = flight.id

                when {
                    findUnloadedBox is Optional.Success -> //коробка уже выгружена из машины
                        // TODO: 01.07.2021 сравнить с тем ПВЗ где ее выгрузили
                        // TODO: 01.07.2021 вызвать метод PVZ scan
                        return@flatMap boxAlreadyUnloaded(findUnloadedBox)

                    findReturnBox is Optional.Success -> //коробка уже добавлена к возврату
                        // TODO: 01.07.2021 вызвать метод PVZ scan
                        return@flatMap boxAlreadyReturn(findReturnBox)

                    findAttachedBox is Optional.Success -> { //коробка в списке доставки
                        with(findAttachedBox) {
                            if (currentOfficeId == data.dstOffice.id) { //коробка принадлежит ПВЗ для выгрузки
                                return@flatMap loadUnloadBoxByList(barcodeScanned,
                                    updatedAt,
                                    flightId,
                                    isManualInput,
                                    currentOfficeId)
                            } else {
                                return@flatMap boxNotBelongPvz(barcodeScanned,
                                    isManualInput,
                                    updatedAt,
                                    currentOfficeId,
                                    flightId,
                                    data.dstOffice.fullAddress)
                            }
                        }
                    }

                    findPvzMatchingBox is Optional.Success -> { //коробка в списке на возврат с ПВЗ - забираем коробку
                        with(findPvzMatchingBox) {
                            return@flatMap loadReturnBoxByList(barcodeScanned,
                                updatedAt,
                                flightId,
                                isManualInput,
                                currentOfficeId)
                        }
                    }

                    else -> {
                        return@flatMap appRemoteRepository.boxInfo(flightId.toString())
                            .flatMapObservable {
                                if (it.box is Optional.Success) { //получена информация по коробке
                                    if (currentOfficeId == it.box.data.dstOffice.id) { //коробку нужно выгрузить на ПВЗ
                                        it.box.loadUnloadBoxByInfo(barcodeScanned,
                                            updatedAt,
                                            flightId,
                                            isManualInput,
                                            currentOfficeId)
                                    } else if (currentOfficeId == it.box.data.srcOffice.id) { //коробку нужно забрать с ПВЗ
                                        it.box.loadReturnBoxByInfo(barcodeScanned,
                                            updatedAt,
                                            flightId,
                                            isManualInput,
                                            currentOfficeId)
                                    } else { //коробка не принадлежит ПВЗ
                                        boxNotBelongPvz(barcodeScanned,
                                            isManualInput,
                                            updatedAt,
                                            currentOfficeId,
                                            flightId,
                                            it.box.data.dstOffice.fullAddress)
                                    }
                                } else { //информация по коробке не найдена
                                    appRemoteRepository.putBoxTracker(barcodeScanned,
                                        isManualInput,
                                        updatedAt,
                                        currentOfficeId,
                                        flightId)
                                        .onErrorComplete()
                                        .andThen(Observable.just(UnloadingData.BoxEmptyInfo(
                                            barcodeScanned)))

                                }
                            }
                            .compose(rxSchedulerFactory.applyObservableSchedulers())
                    }
                }
            }
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    private fun boxNotBelongPvz(
        barcodeScanned: String,
        isManualInput: Boolean,
        updatedAt: String,
        currentOfficeId: Int,
        flightId: Int,
        fullAddress: String,
    ) = appRemoteRepository.putBoxTracker(barcodeScanned,
        isManualInput,
        updatedAt,
        currentOfficeId,
        flightId)
        .onErrorComplete() // TODO: 23.06.2021 реализовать обработчик ошибок
        .andThen(Observable.just(UnloadingData.BoxDoesNotBelongPvz(
            barcodeScanned,
            fullAddress)))

    private fun Optional.Success<AttachedBoxEntity>.loadUnloadBoxByList(
        barcodeScanned: String,
        updatedAt: String,
        flightId: Int,
        isManualInput: Boolean,
        currentOfficeId: Int,
    ): Observable<UnloadingData.BoxUnloadAdded> {
        val saveUnloadedBox =
            appLocalRepository.saveFlightBox(convertToFlightBoxEntity(barcodeScanned, updatedAt))
        val removeBoxFromBalance =
            removeBoxFromPvzBalance(flightId.toString(), //снятие с баланса
                data.barcode,
                isManualInput,
                updatedAt,
                currentOfficeId)
        val deleteAttachedBox = appLocalRepository.deleteAttachedBox(data)
        val boxUnloadAdded =
            Observable.just(UnloadingData.BoxUnloadAdded(data.barcode))
        val switchScreenUnloading = switchScreenUnloading(currentOfficeId)

        return saveUnloadedBox
            .andThen(removeBoxFromBalance)
            .andThen(deleteAttachedBox)
            .andThen(switchScreenUnloading)
            .andThen(boxUnloadAdded)
    }

    private fun Optional.Success<AttachedBoxEntity>.convertToFlightBoxEntity(
        barcodeScanned: String,
        updatedAt: String,
    ) = FlightBoxEntity(
        barcode = barcodeScanned,
        updatedAt = updatedAt,
        status = BoxStatus.REMOVED_FROM_FLIGHT.ordinal,
        onBoard = false,
        srcOffice = FlightSrcOfficeEntity(
            id = data.srcOffice.id,
            name = data.srcOffice.name,
            fullAddress = data.srcOffice.fullAddress,
            longitude = data.srcOffice.longitude,
            latitude = data.srcOffice.latitude),
        dstOffice = FlightDstOfficeEntity(
            id = data.dstOffice.id,
            name = data.dstOffice.name,
            fullAddress = data.dstOffice.fullAddress,
            longitude = data.dstOffice.longitude,
            latitude = data.dstOffice.latitude)
    )

    private fun Optional.Success<BoxInfoEntity>.loadUnloadBoxByInfo(
        barcodeScanned: String,
        updatedAt: String,
        flightId: Int,
        isManualInput: Boolean,
        currentOfficeId: Int,
    ): Observable<UnloadingData.BoxUnloadAdded> {
        val saveUnloadedBox =
            appLocalRepository.saveFlightBox(convertToFlightBoxEntityFromInfoEntity(barcodeScanned,
                updatedAt))
        val removeBoxFromBalance =
            removeBoxFromPvzBalance(flightId.toString(), //снятие с баланса
                data.barcode,
                isManualInput,
                updatedAt,
                currentOfficeId)
        val boxUnloadAdded =
            Observable.just(UnloadingData.BoxUnloadAdded(data.barcode))
        val switchScreenUnloading = switchScreenUnloading(currentOfficeId)

        return saveUnloadedBox
            .andThen(removeBoxFromBalance)
            .andThen(switchScreenUnloading)
            .andThen(boxUnloadAdded)
    }

    private fun Optional.Success<BoxInfoEntity>.convertToFlightBoxEntityFromInfoEntity(
        barcodeScanned: String,
        updatedAt: String,
    ) = FlightBoxEntity(
        barcode = barcodeScanned,
        updatedAt = updatedAt,
        status = BoxStatus.REMOVED_FROM_FLIGHT.ordinal,
        onBoard = false,
        srcOffice = FlightSrcOfficeEntity(
            id = data.srcOffice.id,
            name = data.srcOffice.name,
            fullAddress = data.srcOffice.fullAddress,
            longitude = data.srcOffice.longitude,
            latitude = data.srcOffice.latitude),
        dstOffice = FlightDstOfficeEntity(
            id = data.dstOffice.id,
            name = data.dstOffice.name,
            fullAddress = data.dstOffice.fullAddress,
            longitude = data.dstOffice.longitude,
            latitude = data.dstOffice.latitude)
    )

    private fun Optional.Success<PvzMatchingBoxEntity>.loadReturnBoxByList(
        barcodeScanned: String,
        updatedAt: String,
        flightId: Int,
        isManualInput: Boolean,
        currentOfficeId: Int,
    ): Observable<UnloadingData.BoxReturnAdded> {
        val saveUnloadedBox =
            appLocalRepository.saveFlightBox(FlightBoxEntity(
                barcode = barcodeScanned,
                updatedAt = updatedAt,
                status = BoxStatus.REMOVED_FROM_FLIGHT.ordinal,
                onBoard = false,
                srcOffice = FlightSrcOfficeEntity(
                    id = data.srcOffice.id,
                    name = data.srcOffice.name,
                    fullAddress = data.srcOffice.fullAddress,
                    longitude = data.srcOffice.longitude,
                    latitude = data.srcOffice.latitude),
                dstOffice = FlightDstOfficeEntity(
                    id = data.dstOffice.id,
                    name = data.dstOffice.name,
                    fullAddress = data.dstOffice.fullAddress,
                    longitude = data.dstOffice.longitude,
                    latitude = data.dstOffice.latitude)
            ))
        val putBoxToPvzBalance =
            putBoxToPvzBalance(flightId.toString(), //постановка на баланс коробки с ПВЗ
                data.barcode,
                isManualInput,
                updatedAt,
                currentOfficeId)
        val deletePvzMatchingBox =
            appLocalRepository.deletePvzMatchingBox(data)
        val boxReturnAdded =
            Observable.just(UnloadingData.BoxReturnAdded(barcodeScanned))
        val switchScreenUnloading = switchScreenUnloading(currentOfficeId)

        return putBoxToPvzBalance
            .andThen(saveUnloadedBox)
            .andThen(deletePvzMatchingBox)
            .andThen(switchScreenUnloading)
            .andThen(boxReturnAdded)
    }

    private fun Optional.Success<BoxInfoEntity>.loadReturnBoxByInfo(
        barcodeScanned: String,
        updatedAt: String,
        flightId: Int,
        isManualInput: Boolean,
        currentOfficeId: Int,
    ): Observable<UnloadingData.BoxReturnAdded> {
        val saveUnloadedBox =
            appLocalRepository.saveFlightBox(FlightBoxEntity(
                barcode = barcodeScanned,
                updatedAt = updatedAt,
                status = BoxStatus.REMOVED_FROM_FLIGHT.ordinal,
                onBoard = false,
                srcOffice = FlightSrcOfficeEntity(
                    id = data.srcOffice.id,
                    name = data.srcOffice.name,
                    fullAddress = data.srcOffice.fullAddress,
                    longitude = data.srcOffice.longitude,
                    latitude = data.srcOffice.latitude),
                dstOffice = FlightDstOfficeEntity(
                    id = data.dstOffice.id,
                    name = data.dstOffice.name,
                    fullAddress = data.dstOffice.fullAddress,
                    longitude = data.dstOffice.longitude,
                    latitude = data.dstOffice.latitude)
            ))
        val putBoxToPvzBalance =
            putBoxToPvzBalance(flightId.toString(), //постановка на баланс коробки с ПВЗ
                data.barcode,
                isManualInput,
                updatedAt,
                currentOfficeId)
        val boxReturnAdded =
            Observable.just(UnloadingData.BoxReturnAdded(barcodeScanned))
        val switchScreenUnloading = switchScreenUnloading(currentOfficeId)

        return saveUnloadedBox
            .andThen(putBoxToPvzBalance)
            .andThen(switchScreenUnloading)
            .andThen(boxReturnAdded)
    }

    private fun boxAlreadyReturn(findReturnBox: Optional.Success<FlightBoxEntity>) =
        Observable.just(with(findReturnBox.data) {
            UnloadingData.BoxAlreadyReturn(barcode)
        })

    private fun boxAlreadyUnloaded(findUnloadedBox: Optional.Success<FlightBoxEntity>) =
        Observable.just(with(findUnloadedBox.data) {
            UnloadingData.BoxAlreadyUnloaded(barcode)
        })

    override fun observeCountUnloadReturnedBox(currentOfficeId: Int): Observable<Int> {
        return Flowable.combineLatest(
            appLocalRepository.observeUnloadedFlightBoxesByOfficeId(currentOfficeId),
            appLocalRepository.observeReturnedFlightBoxesByOfficeId(currentOfficeId),
            { unloadedBoxes, returnBoxes -> unloadedBoxes.size + returnBoxes.size })
            .toObservable()
            .filter { it > 0 }
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    override fun observeAttachedBoxes(currentOfficeId: Int): Observable<List<AttachedBoxEntity>> {
        return appLocalRepository.observeAttachedBoxes(currentOfficeId)
            .toObservable()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    override fun observeUnloadedBoxes(currentOfficeId: Int): Observable<List<FlightBoxEntity>> {
        return appLocalRepository.observeUnloadedFlightBoxesByOfficeId(currentOfficeId)
            .toObservable()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    override fun removeReturnBoxes(currentOfficeId: Int, checkedBoxes: List<String>): Completable {
        return flight().flatMapCompletable { removeBoxes(it, currentOfficeId, checkedBoxes) }
            .andThen(appLocalRepository.findReturnFlightBoxes(checkedBoxes))
            .flatMapCompletable { appLocalRepository.deleteReturnFlightBoxes(it) }
            .compose(rxSchedulerFactory.applyCompletableSchedulers())
    }

    private fun removeBoxes(
        flightEntity: FlightEntity,
        dstOfficeId: Int,
        checkedBoxes: List<String>,
    ) =
        appRemoteRepository.removeBoxesFromFlight(flightEntity.id.toString(),
            false,
            timeManager.getOffsetLocalTime(),
            dstOfficeId,
            checkedBoxes)

    private fun boxDefinitionResult(
        barcode: String,
        isManual: Boolean,
        currentOfficeId: Int,
    ): Single<BoxDefinitionResult> {
        return Single.zip(
            flight(), //рейс
            findUnloadedBox(barcode, currentOfficeId), //коробка есть в списке выгруженных
            findReturnedBox(barcode, currentOfficeId), //коробка есть в списке на возврат
            findAttachedBox(barcode), //коробка есть в списке доставки
            findPvzMatchingBox(barcode), //коробка есть в списке возвратных коробок ПВЗ
            { flight, findUnloadedBox, findReturnBox, findAttachedBox, findPvzMatchingBox ->
                BoxDefinitionResult(flight,
                    findUnloadedBox,
                    findReturnBox,
                    findAttachedBox,
                    findPvzMatchingBox,
                    barcode,
                    isManual)
            }
        ).compose(rxSchedulerFactory.applySingleSchedulers())
    }

    private fun flight() = appLocalRepository.readFlight()

    private fun findUnloadedBox(barcode: String, currentOfficeId: Int) =
        appLocalRepository.findUnloadedFlightBox(barcode, currentOfficeId)

    private fun findReturnedBox(barcode: String, currentOfficeId: Int) =
        appLocalRepository.findReturnedFlightBox(barcode, currentOfficeId)

    private fun findAttachedBox(barcode: String) = appLocalRepository.findAttachedBox(barcode)

    private fun findPvzMatchingBox(barcode: String) = appLocalRepository.findPvzMatchingBox(barcode)

    private fun removeBoxFromPvzBalance(
        flightId: String,
        barcode: String,
        isManualInput: Boolean,
        updatedAt: String,
        currentOffice: Int,
    ) = appRemoteRepository.removeBoxFromPvzBalance(
        flightId,
        barcode,
        isManualInput,
        updatedAt,
        currentOffice)
        .compose(rxSchedulerFactory.applyCompletableSchedulers())

    private fun putBoxToPvzBalance(
        flightId: String,
        barcode: String,
        isManualInput: Boolean,
        updatedAt: String,
        currentOffice: Int,
    ) = appRemoteRepository.putBoxToPvzBalance(
        flightId,
        barcode,
        isManualInput,
        updatedAt,
        currentOffice)
        .compose(rxSchedulerFactory.applyCompletableSchedulers())

    override fun observeUnloadedAndAttachedBoxes(currentOfficeId: Int): Observable<Pair<List<FlightBoxEntity>, List<AttachedBoxEntity>>> {
        return Flowable.combineLatest(appLocalRepository.observeUnloadedFlightBoxesByOfficeId(
            currentOfficeId),
            appLocalRepository.observeAttachedBoxes(currentOfficeId),
            { unloaded, attached -> Pair(unloaded, attached) })
            .toObservable()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    override fun observeReturnBoxes(currentOfficeId: Int): Observable<List<FlightBoxEntity>> {
        return appLocalRepository.observeReturnedFlightBoxesByOfficeId(currentOfficeId)
            .toObservable()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    override fun scannerAction(scannerAction: ScannerAction) {
        scannerRepository.scannerAction(scannerAction)
    }

    override fun completeUnloading(): Completable {
        return switchScreenInTransit().compose(rxSchedulerFactory.applyCompletableSchedulers())
    }

    override fun officeNameById(currentOfficeId: Int): Single<String> {
        return appLocalRepository.findFlightOffice(currentOfficeId).map { it.name }
            .compose(rxSchedulerFactory.applySingleSchedulers())
    }

    private fun switchScreenInTransit(): Completable {
        return screenManager.saveState(FlightStatus.INTRANSIT)
    }

    private fun switchScreenUnloading(dstOfficeId: Int): Completable {
        return screenManager.saveState(FlightStatus.UNLOADING, dstOfficeId)
    }

}
