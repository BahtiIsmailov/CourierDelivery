package com.wb.logistics.ui.unloading.domain

import com.wb.logistics.db.AppLocalRepository
import com.wb.logistics.db.Optional
import com.wb.logistics.db.entity.attachedboxes.AttachedBoxEntity
import com.wb.logistics.db.entity.returnboxes.ReturnBoxEntity
import com.wb.logistics.db.entity.returnboxes.ReturnCurrentOfficeEntity
import com.wb.logistics.db.entity.unloadedboxes.UnloadedBoxEntity
import com.wb.logistics.db.entity.unloadedboxes.UnloadedCurrentOfficeEntity
import com.wb.logistics.network.api.app.AppRemoteRepository
import com.wb.logistics.network.api.app.FlightStatus
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
                val updatedAt = timeManager.getOffsetLocalTime()

                val flightId = when (flight) {
                    is Optional.Success -> flight.data.id
                    is Optional.Empty -> 0
                }
                // TODO: 29.04.2021 добавить конвертер состояния в случае 0 рейса

                when {
                    findUnloadedBox is Optional.Success -> //коробка уже выгружена из машины
                        return@flatMap Observable.just(with(findUnloadedBox.data) {
                            UnloadingData.BoxAlreadyUnloaded(barcode)
                        })

                    findReturnBox is Optional.Success -> //коробка уже добавлена к возврату
                        return@flatMap Observable.just(with(findReturnBox.data) {
                            UnloadingData.BoxAlreadyReturn(barcode)
                        })

                    findAttachedBox is Optional.Success -> { //коробка в списке доставки
                        with(findAttachedBox) {
                            if (dstOfficeId == data.dstOffice.id) { //коробка принадлежит ПВЗ
                                // TODO: 27.04.2021 добавить коробку в базу
                                val attachAt = findAttachedBox.data.updatedAt
                                val saveUnloadedBox =
                                    appLocalRepository.saveUnloadedBox(UnloadedBoxEntity(
                                        flightId,
                                        isManualInput,
                                        barcodeScanned,
                                        updatedAt,
                                        attachAt,
                                        UnloadedCurrentOfficeEntity(dstOfficeId)))
                                val removeBoxFromBalance =
                                    removeBoxFromBalance(flightId.toString(), //снятие с баланса
                                        data.barcode,
                                        isManualInput,
                                        updatedAt,
                                        dstOfficeId)
                                val deleteAttachedBox = appLocalRepository.deleteAttachedBox(data)
                                val boxUnloadAdded =
                                    Observable.just(UnloadingData.BoxUnloadAdded(data.barcode))
                                val switchScreen = switchScreenUnloading(dstOfficeId)

                                return@flatMap saveUnloadedBox
                                    .andThen(removeBoxFromBalance)
                                    .andThen(deleteAttachedBox)
                                    .andThen(switchScreen)
                                    .andThen(boxUnloadAdded)
                            } else {
                                return@flatMap Observable.just(UnloadingData.BoxDoesNotBelongPoint(
                                    data.barcode,
                                    data.dstFullAddress))
                            }
                        }
                    }

                    findAttachedBox is Optional.Empty -> { //коробки нет в списке доставки - принятие на возврат
                        val saveReturnBox = saveReturnBox(flightId,
                            isManualInput,
                            barcodeScanned,
                            updatedAt,
                            dstOfficeId)
                        val pvzBoxToBalanceRemote = pvzBoxToBalanceRemote(flightId.toString(),
                            barcodeScanned,
                            isManualInput,
                            updatedAt,
                            dstOfficeId)
                        val boxReturnAdded =
                            Observable.just(UnloadingData.BoxReturnAdded(barcodeScanned))

                        return@flatMap saveReturnBox.andThen(pvzBoxToBalanceRemote)
                            .andThen(boxReturnAdded)
                    }
                    else -> return@flatMap Observable.just(UnloadingData.Empty)
                }
            }
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    private fun pvzBoxToBalanceRemote(
        flightId: String,
        barcode: String,
        isManualInput: Boolean,
        updatedAt: String,
        currentOffice: Int,
    ) = appRemoteRepository.pvzBoxToBalance(
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
    ) = appLocalRepository.saveReturnBox(ReturnBoxEntity(flightId,
        isManualInput,
        barcodeScanned,
        updateAt,
        ReturnCurrentOfficeEntity(dstOfficeId)))

    override fun observeAttachedBoxes(dstOfficeId: Int): Observable<List<AttachedBoxEntity>> {
        return appLocalRepository.observeAttachedBoxes(dstOfficeId)
            .toObservable()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    override fun observeUnloadedBoxes(dstOfficeId: Int): Observable<List<UnloadedBoxEntity>> {
        return appLocalRepository.observeUnloadedBoxesByDstOfficeId(dstOfficeId)
            .toObservable()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    override fun removeReturnBoxes(checkedBoxes: List<String>): Completable {
        return appLocalRepository.findReturnBoxes(checkedBoxes)
            .flatMapCompletable { returnBoxes ->
                Observable.fromIterable(returnBoxes)
                    .flatMapCompletable {
                        removeReturnBoxRemote(it).andThen(deleteReturnBox(it))
                    }
            }.compose(rxSchedulerFactory.applyCompletableSchedulers())
    }

    private fun removeReturnBoxRemote(returnBoxEntity: ReturnBoxEntity) =
        with(returnBoxEntity) {
            appRemoteRepository.removeBoxFromFlight(
                flightId.toString(),
                barcode,
                isManualInput,
                updatedAt,
                currentOffice.id)
                .onErrorComplete() // TODO: 29.04.2021 реализовать конвертер ошибки
        }

    private fun deleteReturnBox(returnBoxEntity: ReturnBoxEntity) =
        appLocalRepository.deleteReturnBox(returnBoxEntity).onErrorComplete()

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

    private fun flight() = appLocalRepository.readFlight()

    private fun findAttachedBox(barcode: String) = appLocalRepository.findAttachedBox(barcode)

    private fun findUnloadedBox(barcode: String) = appLocalRepository.findUnloadedBox(barcode)

    private fun findReturnBox(barcode: String) = appLocalRepository.findReturnBox(barcode)

    private fun removeBoxFromBalance(
        flightId: String,
        barcode: String,
        isManualInput: Boolean,
        updatedAt: String,
        currentOffice: Int,
    ) = appRemoteRepository.removeBoxFromBalance(
        flightId,
        barcode,
        isManualInput,
        updatedAt,
        currentOffice)
        .onErrorComplete() // TODO: 29.04.2021 реализовать конвертер ошибки
        .compose(rxSchedulerFactory.applyCompletableSchedulers())

    override fun observeUnloadedAndAttachedBoxes(dstOfficeId: Int): Observable<Pair<List<UnloadedBoxEntity>, List<AttachedBoxEntity>>> {
        return Flowable.combineLatest(appLocalRepository.observeUnloadedBoxesByDstOfficeId(
            dstOfficeId),
            appLocalRepository.observeAttachedBoxes(dstOfficeId),
            { unloaded, attached -> Pair(unloaded, attached) })
            .toObservable()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    override fun observeReturnBoxes(dstOfficeId: Int): Observable<List<ReturnBoxEntity>> {
        return appLocalRepository.observedReturnBoxesByDstOfficeId(dstOfficeId).toObservable()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    override fun scannerAction(scannerAction: ScannerAction) {
        scannerRepository.scannerAction(scannerAction)
    }

    override fun completeUnloading(dstOfficeId: Int): Completable {
        return switchScreenInTransit().andThen(appLocalRepository.changeFlightOfficeUnloading(
            dstOfficeId,
            true,
            ""))
            .compose(rxSchedulerFactory.applyCompletableSchedulers())
    }

    private fun switchScreenInTransit(): Completable {
        return screenManager.saveState(FlightStatus.INTRANSIT)
    }

    private fun switchScreenUnloading(dstOfficeId: Int): Completable {
        return screenManager.saveState(FlightStatus.UNLOADING, dstOfficeId)
    }

}
