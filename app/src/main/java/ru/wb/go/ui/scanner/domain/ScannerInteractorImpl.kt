package ru.wb.go.ui.scanner.domain

import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Action
import io.reactivex.subjects.PublishSubject
import ru.wb.go.network.rx.RxSchedulerFactory
import java.util.concurrent.TimeUnit


class ScannerInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val scannerRepository: ScannerRepository,
) : ScannerInteractor {

    private val holdSplashSubject = PublishSubject.create<Action>()
    private val prolongHoldSubject = PublishSubject.create<Action>()
    private var timerDisposable: Disposable? = null

    init {
        startTimer()
    }

    override fun barcodeScanned(barcode: String) {
        scannerRepository.scannerAction(ScannerAction.ScanResult(barcode))
    }

    override fun holdSplashUnlock() {
        scannerRepository.scannerAction(ScannerAction.HoldSplashUnlock)
    }

    override fun prolongHoldTimer() {
        startTimer()
        prolongHoldSubject.onNext(Action { })
    }

    override fun observeScannerState(): Observable<ScannerState> {
        return scannerRepository.observeScannerState()
            .doOnNext {
                if (it is ScannerState.StartScan) startTimer()
                else if (it is ScannerState.StopScan || it is ScannerState.HoldScanComplete || it is ScannerState.HoldScanError || it is ScannerState.HoldScanUnknown) stopTimer()
            }
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    private fun startTimer() {
        if (timerDisposable == null) {
            timerDisposable = Observable.timer(HOLD_SCANNER_DELAY, TimeUnit.SECONDS)
                .repeatWhen { repeatHandler -> repeatHandler.flatMap { prolongHoldSubject } }
                .subscribe({
                    scannerRepository.scannerAction(ScannerAction.HoldSplashLock)
                    holdSplashSubject.onNext(Action { })
                }, {})
        }
    }

    private fun stopTimer() {
        if (timerDisposable != null) {
            timerDisposable!!.dispose()
            timerDisposable = null
        }
    }

    override fun observeHoldSplash(): Observable<Action> {
        return holdSplashSubject.compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    companion object {
        const val HOLD_SCANNER_DELAY = 25L
    }

}