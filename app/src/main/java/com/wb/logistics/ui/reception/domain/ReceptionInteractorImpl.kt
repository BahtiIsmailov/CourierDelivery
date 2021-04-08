package com.wb.logistics.ui.reception.domain

import com.wb.logistics.data.AppRepository
import com.wb.logistics.db.SuccessOrEmptyData
import com.wb.logistics.db.entity.flight.FlightEntity
import com.wb.logistics.db.entity.flightboxes.DstOfficeEntity
import com.wb.logistics.db.entity.flightboxes.FlightBoxEntity
import com.wb.logistics.db.entity.flightboxes.FlightBoxScannedEntity
import com.wb.logistics.db.entity.flightboxes.SrcOfficeEntity
import com.wb.logistics.network.rx.RxSchedulerFactory
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject

class ReceptionInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val appRepository: AppRepository,
) : ReceptionInteractor {

    private val actionBarcodeScannedSubject = BehaviorSubject.create<Pair<String, Boolean>>()

    override fun boxScanned(barcode: String, isManualInput: Boolean) {
        actionBarcodeScannedSubject.onNext(Pair(barcode, isManualInput))
    }

    override fun observeScanState(): Observable<ScanBoxData> {
        return actionBarcodeScannedSubject.flatMapSingle { boxDefinitionResult(it) }
            .flatMap { boxDefinition ->

                val flight = boxDefinition.flight
                val flightBox = boxDefinition.flightBox
                val flightBoxHasBeenScanned = boxDefinition.flightBoxHasBeenScanned
                val barcodeScanned = boxDefinition.barcodeScanned

                when {
                    flightBoxHasBeenScanned is SuccessOrEmptyData.Success -> //коробка уже была отсканирована
                        return@flatMap Observable.just(with(flightBoxHasBeenScanned.data) {
                            ScanBoxData.BoxHasBeenAdded(barcode, gate.toString())
                        })
                    flight is SuccessOrEmptyData.Success -> //данные по рейсу актуальны
                        when (flightBox) {
                            is SuccessOrEmptyData.Success ->  //коробка принадлежит рейсу
                                return@flatMap saveFlightBox(
                                    flightId = flight.data.id.toString(),
                                    barcode = flightBox.data.barcode,
                                    isManual = boxDefinition.isManual,
                                    officeId = flightBox.data.srcOffice.id,
                                    flightBox = flightBox,
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
                ScanBoxData.Empty
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

    private fun saveFlightBox(
        flightId: String,
        barcode: String,
        isManual: Boolean,
        officeId: Int,
        flightBox: SuccessOrEmptyData.Success<FlightBoxEntity>,
        gate: Int,
    ): Observable<ScanBoxData> {
        val boxToFlight = boxToFlight(
            flightId,
            barcode,
            isManual,
            officeId)
        val saveBoxInfo =
            saveFlightBoxScanned(convertFlightBoxScanned(flightBox.data, gate, isManual))
        val boxAdded = boxAdded(barcode, gate.toString())
        return boxToFlight
            .andThen(saveBoxInfo)
            .andThen(boxAdded)
            .toObservable()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    private fun convertFlightBoxScanned(
        flightBoxEntity: FlightBoxEntity,
        gate: Int,
        isManual: Boolean,
    ) = with(flightBoxEntity) {
        FlightBoxScannedEntity(
            flightId = flightId,
            barcode = barcode,
            gate = gate,
            srcOffice = SrcOfficeEntity(srcOffice.id),
            dstOffice = DstOfficeEntity(dstOffice.id),
            smID = smID,
            isManualInput = isManual)
    }

    override fun removeFlightBoxes(checkedBoxes: List<Boolean>) {

    }

    private fun boxDefinitionResult(param: Pair<String, Boolean>): Single<BoxDefinitionResult> {
        val barcodeScanned = param.first
        val isManual = param.second
        return Single.zip(
            flight(), //рейс
            findFlightBoxScanned(barcodeScanned), //коробка уже добавлена
            findFlightBox(barcodeScanned), //коробка привязана к рейсу
            { flight, findFlightBoxScanned, findFlightBox ->
                BoxDefinitionResult(flight,
                    findFlightBoxScanned,
                    findFlightBox,
                    barcodeScanned,
                    isManual)
            }
        ).compose(rxSchedulerFactory.applySingleSchedulers())
    }

    private fun flight() = appRepository.readFlight()

    private fun findFlightBoxScanned(barcode: String) = appRepository.findFlightBoxScanned(barcode)

    private fun findFlightBox(barcode: String) = appRepository.findFlightBox(barcode)

    private fun boxToFlight(
        flight: String,
        barcode: String,
        isManualInput: Boolean,
        currentOffice: Int,
    ) = appRepository.boxToFlight(
        flight,
        barcode,
        isManualInput,
        currentOffice)

    private fun saveFlightBoxScanned(FlightBoxScanned: FlightBoxScannedEntity) =
        appRepository.saveFlightBoxScanned(FlightBoxScanned)

    private fun boxAdded(barcode: String, gate: String) =
        Single.just<ScanBoxData>(ScanBoxData.BoxAdded(barcode, gate))

    private fun infoBox(barcode: String) = appRepository.boxInfo(barcode)

    override fun observeFlightBoxes(): Observable<List<FlightBoxScannedEntity>> {
        return appRepository.observeFlightBoxesScanned().toObservable()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

}