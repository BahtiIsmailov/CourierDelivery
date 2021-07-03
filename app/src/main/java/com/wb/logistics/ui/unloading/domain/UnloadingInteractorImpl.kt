package com.wb.logistics.ui.unloading.domain

import com.wb.logistics.db.AppLocalRepository
import com.wb.logistics.db.Optional
import com.wb.logistics.db.entity.flighboxes.*
import com.wb.logistics.db.entity.flight.FlightEntity
import com.wb.logistics.db.entity.pvzmatchingboxes.PvzMatchingBoxEntity
import com.wb.logistics.network.api.app.AppRemoteRepository
import com.wb.logistics.network.api.app.FlightStatus
import com.wb.logistics.network.api.app.entity.boxinfo.BoxInfoEntity
import com.wb.logistics.network.exceptions.BadRequestException
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
            .flatMapSingle { boxDefinitionResult(it.first, it.second) }
            .flatMap { boxDefinition ->

                val flight = boxDefinition.flight
                val findFlightBox = boxDefinition.findFlightBox
                val findPvzMatchingBox = boxDefinition.findPvzMatchingBox
                val barcodeScanned = boxDefinition.barcodeScanned
                val isManualInput = boxDefinition.isManualInput
                val updatedAt = timeManager.getOffsetLocalTime()
                val flightId = flight.id

                when {
                    findFlightBox is Optional.Success -> {
                        with(findFlightBox) {
                            if (currentOfficeId == data.dstOffice.id) { // коробка для выгрузки на ПВЗ снятие с баланса
                                return@flatMap unloadTakeOnFlightBox(updatedAt,
                                    flightId,
                                    isManualInput,
                                    currentOfficeId)
                            } else if (currentOfficeId == data.srcOffice.id) { //возвратная коробка значит это повторное сканирование постановка на баланс
                                val putBoxToPvzBalance =
                                    loadPvzScanRemote(flightId.toString(), //постановка на баланс коробки с ПВЗ
                                        data.barcode,
                                        isManualInput,
                                        updatedAt,
                                        currentOfficeId)
                                val boxReturnAdded =
                                    Observable.just(UnloadingData.BoxReturnAdded(barcodeScanned))
                                return@flatMap putBoxToPvzBalance.andThen(boxReturnAdded)
                            } else { //коробка с другого ПВЗ вернуть в машину
                                return@flatMap boxNotBelongPvzTracker(barcodeScanned,
                                    isManualInput,
                                    updatedAt,
                                    currentOfficeId,
                                    flightId,
                                    data.dstOffice.fullAddress)
                            }
                        }
                    }

                    findPvzMatchingBox is Optional.Success -> {
                        with(findPvzMatchingBox) {
                            if (currentOfficeId == data.srcOffice.id) { //коробка принадлежит ПВЗ постановка на баланс
                                return@flatMap loadReturnBoxByList(barcodeScanned,
                                    updatedAt,
                                    flightId,
                                    isManualInput,
                                    currentOfficeId)
                            } else { //коробка в списке для возврата, но не принадлежит ПВЗ
                                return@flatMap boxNotBelongPvzTracker(barcodeScanned,
                                    isManualInput,
                                    updatedAt,
                                    currentOfficeId,
                                    flightId,
                                    data.dstOffice.fullAddress)
                            }
                        }
                    }

                    else -> { //информация по ШК не найдена
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
                                        boxNotBelongPvzTracker(barcodeScanned,
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
                                        .andThen(Observable.just(UnloadingData.BoxInfoEmpty(
                                            barcodeScanned)))

                                }
                            }
                            .compose(rxSchedulerFactory.applyObservableSchedulers())
                    }
                }
            }
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    private fun boxNotBelongPvzTracker(
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
        .onErrorResumeNext { //оборачиваем сетевую ошибку 400
            if (it is BadRequestException) Completable.complete()
            else throw it
        }
        .andThen(boxDoesNotBelongPvz(barcodeScanned, fullAddress))
        .compose(rxSchedulerFactory.applyObservableSchedulers())

    private fun boxDoesNotBelongPvz(barcodeScanned: String, fullAddress: String) =
        Observable.just(UnloadingData.BoxDoesNotBelongPvz(barcodeScanned, fullAddress))

    private fun Optional.Success<FlightBoxEntity>.unloadTakeOnFlightBox(
        updatedAt: String,
        flightId: Int,
        isManualInput: Boolean,
        currentOfficeId: Int,
    ): Observable<UnloadingData.BoxUnloadAdded> {
        val removeBoxFromBalance =
            unloadPvzScanRemote(flightId.toString(), //снятие с баланса
                data.barcode,
                isManualInput,
                updatedAt,
                currentOfficeId)
        val saveFlightBox =
            appLocalRepository.saveFlightBox(data.copy(
                updatedAt = updatedAt,
                status = BoxStatus.DELIVERED.ordinal,
                onBoard = false))
        val boxUnloadAdded =
            Observable.just(UnloadingData.BoxUnloadAdded(data.barcode))
        val switchScreenUnloading = switchScreenUnloading(currentOfficeId)

        return removeBoxFromBalance
            .andThen(saveFlightBox)
            .andThen(switchScreenUnloading)
            .andThen(boxUnloadAdded)
    }

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
            unloadPvzScanRemote(flightId.toString(), //снятие с баланса
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
        val putBoxToPvzBalance =
            loadPvzScanRemote(flightId.toString(), //постановка на баланс коробки с ПВЗ
                data.barcode,
                isManualInput,
                updatedAt,
                currentOfficeId)
        val saveFlightBox =
            appLocalRepository.saveFlightBox(convertFlightBoxEntity(updatedAt, true))
        val switchScreenUnloading = switchScreenUnloading(currentOfficeId)
        val boxReturnAdded =
            Observable.just(UnloadingData.BoxReturnAdded(barcodeScanned))

        return putBoxToPvzBalance
            .andThen(saveFlightBox)
            .andThen(switchScreenUnloading)
            .andThen(boxReturnAdded)
    }

    private fun Optional.Success<PvzMatchingBoxEntity>.convertFlightBoxEntity(
        updatedAt: String,
        onBoard: Boolean,
    ) =
        FlightBoxEntity(
            barcode = data.barcode,
            updatedAt = updatedAt,
            status = BoxStatus.REMOVED_FROM_FLIGHT.ordinal,
            onBoard = onBoard,
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
            loadPvzScanRemote(flightId.toString(), //постановка на баланс коробки с ПВЗ
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

    override fun observeCountUnloadReturnedBoxAndSwitchScreen(currentOfficeId: Int): Observable<Int> {
        return Flowable.combineLatest(
            appLocalRepository.observeUnloadedFlightBoxesByOfficeId(currentOfficeId),
            appLocalRepository.observeReturnedFlightBoxesByOfficeId(currentOfficeId),
            { unloadedBoxes, returnBoxes -> unloadedBoxes.size + returnBoxes.size })
            .toObservable()
            .filter { it > 0 }
            .flatMap { switchScreenUnloading(currentOfficeId).andThen(Observable.just(it)) }
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }


    override fun observeAttachedBoxes(currentOfficeId: Int): Observable<List<FlightBoxEntity>> {
        return appLocalRepository.observeTakeOnFlightBoxesByOfficeId(currentOfficeId)
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
            .flatMapCompletable { appLocalRepository.deleteFlightBoxes(it) }
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
    ): Single<BoxDefinitionResult> {
        return Single.zip(
            flight(), //рейс
            findTakeOnFlightBox(barcode), //коробка есть в списке доставки
            findPvzMatchingBox(barcode), //коробка есть в списке возвратных коробок ПВЗ
            { flight, findAttachedBox, findPvzMatchingBox -> //findUnloadedPvzBox, findReturnPvzBox,
                BoxDefinitionResult(flight,
                    findAttachedBox,
                    findPvzMatchingBox,
                    barcode,
                    isManual)
            }
        ).compose(rxSchedulerFactory.applySingleSchedulers())
    }

    private fun flight() = appLocalRepository.readFlight()

    private fun findTakeOnFlightBox(barcode: String) = appLocalRepository.findFlightBox(barcode)

    private fun findPvzMatchingBox(barcode: String) = appLocalRepository.findPvzMatchingBox(barcode)

    private fun unloadPvzScanRemote(
        flightId: String,
        barcode: String,
        isManualInput: Boolean,
        updatedAt: String,
        currentOffice: Int,
    ) = appRemoteRepository.unloadPvzScan(
        flightId,
        barcode,
        isManualInput,
        updatedAt,
        currentOffice)
        .compose(rxSchedulerFactory.applyCompletableSchedulers())

    private fun loadPvzScanRemote(
        flightId: String,
        barcode: String,
        isManualInput: Boolean,
        updatedAt: String,
        currentOffice: Int,
    ) = appRemoteRepository.loadPvzScan(
        flightId,
        barcode,
        isManualInput,
        updatedAt,
        currentOffice)
        .compose(rxSchedulerFactory.applyCompletableSchedulers())

    override fun observeUnloadedAndTakeOnFlightBoxes(currentOfficeId: Int): Observable<FlightUnloadedAndUnloadCountEntity> {
        return appLocalRepository.observeUnloadedAndTakeOnFlightBoxes(currentOfficeId)
            .toObservable()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    override fun observeReturnBoxes(currentOfficeId: Int): Observable<List<FlightBoxEntity>> {
        return appLocalRepository.observeReturnedFlightBoxesByOfficeId(currentOfficeId)
            .toObservable()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    override fun observeReturnedAndMatchingBoxes(currentOfficeId: Int): Observable<Pair<List<FlightBoxEntity>, List<PvzMatchingBoxEntity>>> {
        return Flowable.combineLatest(
            appLocalRepository.observeReturnedFlightBoxesByOfficeId(currentOfficeId),
            appLocalRepository.observePvzMatchingBoxByOfficeId(currentOfficeId),
            { returned, pvzMatching -> Pair(returned, pvzMatching) })
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
