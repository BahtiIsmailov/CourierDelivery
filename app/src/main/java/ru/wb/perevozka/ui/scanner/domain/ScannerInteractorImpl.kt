package ru.wb.perevozka.ui.scanner.domain

import ru.wb.perevozka.network.rx.RxSchedulerFactory
import io.reactivex.Observable

class ScannerInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val scannerRepository: ScannerRepository,
) : ScannerInteractor {

    override fun barcodeScanned(barcode: String) {
        scannerRepository.barcodeScanned(barcode)
    }

    override fun observeScannerAction(): Observable<ScannerAction> {
        return scannerRepository.observeScannerAction()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

}