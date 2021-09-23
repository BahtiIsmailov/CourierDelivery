package ru.wb.perevozka.ui.courierintransit.domain

import io.reactivex.Flowable
import io.reactivex.Observable
import ru.wb.perevozka.app.PREFIX_QR_CODE
import ru.wb.perevozka.db.CourierLocalRepository
import ru.wb.perevozka.db.IntransitTimeRepository
import ru.wb.perevozka.db.entity.courierboxes.CourierIntransitGroupByOfficeEntity
import ru.wb.perevozka.network.rx.RxSchedulerFactory
import ru.wb.perevozka.ui.scanner.domain.ScannerRepository
import ru.wb.perevozka.ui.scanner.domain.ScannerState

class CourierIntransitInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val courierLocalRepository: CourierLocalRepository,
    private val scannerRepository: ScannerRepository,
    private val intransitTimeRepository: IntransitTimeRepository,
) : CourierIntransitInteractor {

    override fun observeBoxesGroupByOffice(): Flowable<List<CourierIntransitGroupByOfficeEntity>> {
        return courierLocalRepository.observeBoxesGroupByOffice()
            .compose(rxSchedulerFactory.applyFlowableSchedulers())
    }

    override fun observeOfficeIdScanProcess(): Observable<Int> {
        return observeOfficeIdScan()
    }

    override fun scannerAction(scannerAction: ScannerState) {
        scannerRepository.scannerState(scannerAction)
    }

    override fun startTime(): Observable<Long> {
        return intransitTimeRepository.startTimer().toObservable()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    private fun observeOfficeIdScan(): Observable<Int> {
        return scannerRepository.observeBarcodeScanned()
            .map { parseQrCode(it) }
            .flatMap { scanOfficeId ->
                courierLoadingScanBoxData().map { it.dstOffices }
                    .map { offices -> offices.find { it.id.toString() == scanOfficeId }?.id ?: 0 }
                    .filter { it != 0 }
                    .toObservable()
            }
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    private fun parseQrCode(qrCode: String): String {
        return getSplitInfo(getInfo(qrCode))[0]
    }

    private fun getSplitInfo(input: String): List<String> {
        return input.split(":")
    }

    private fun getInfo(input: String): String {
        return input.takeLast(input.length - PREFIX_QR_CODE.length)
    }

    private fun courierLoadingScanBoxData() = courierLocalRepository.orderData()

}