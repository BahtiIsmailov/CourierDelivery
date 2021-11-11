package ru.wb.go.ui.scanner.domain

import ru.wb.go.network.rx.RxSchedulerFactory
import io.reactivex.Observable

class ScannerInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val scannerRepository: ScannerRepository,
) : ScannerInteractor {

    override fun barcodeScanned(barcode: String) {
        scannerRepository.scannerAction(barcode)
    }

    override fun observeScannerState(): Observable<ScannerState> {
        return scannerRepository.observeScannerState()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

}