package ru.wb.go.ui.scanner.domain

import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Action
import io.reactivex.subjects.PublishSubject
import ru.wb.go.app.AppPreffsKeys
import ru.wb.go.network.rx.RxSchedulerFactory
import ru.wb.go.utils.managers.SettingsManager
import java.util.concurrent.TimeUnit


class ScannerInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val scannerRepository: ScannerRepository,
    private val settingsManager: SettingsManager,
    override var scannerActionSubject: ScannerAction,
) : ScannerInteractor,test {

    private val holdSplashSubject = PublishSubject.create<Action>()
    private val prolongHoldSubject = PublishSubject.create<Action>()
    private var holdSplashDisposable: Disposable? = null

    companion object{
        const val HOLD_SCANNER_DELAY = 25L
    }

    init {
        startTimer()
    }

    override fun barcodeScanned(barcode: String) {
        scannerActionSubject = scannerRepository.scannerAction(ScannerAction.ScanResult(barcode))
    }



    override fun holdSplashUnlock() {
        scannerActionSubject = scannerRepository.scannerAction(ScannerAction.HoldSplashUnlock)
    }



    override fun prolongHoldTimer() {
        startTimer()
        prolongHoldSubject.onNext(Action { })
    }

    override fun observeScannerState(): Observable<ScannerState> {
        return scannerRepository.observeScannerState()
            .doOnNext {
                if (it is ScannerState.StartScan) startTimer()
                else if (it is ScannerState.StopScan ||
                    it is ScannerState.HoldScanComplete ||
                    it is ScannerState.HoldScanError ||
                    it is ScannerState.HoldScanUnknown
                ) stopTimer()
            }
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    private fun startTimer() {
        if (!settingsManager.getSetting(AppPreffsKeys.SETTING_SANNER_OFF, false)) {
            return
        }
        if (holdSplashDisposable == null) {
            holdSplashDisposable = Observable.timer(HOLD_SCANNER_DELAY, TimeUnit.SECONDS)
                .repeatWhen { repeatHandler -> repeatHandler.flatMap { prolongHoldSubject } }
                .subscribe({
                    scannerActionSubject = scannerRepository.scannerAction(ScannerAction.HoldSplashLock)
                    holdSplashSubject.onNext(Action { })
                }, {})
        }
    }

    private fun stopTimer() {
        if (holdSplashDisposable != null) {
            holdSplashDisposable!!.dispose()
            holdSplashDisposable = null
        }
    }

    override fun observeHoldSplash(): Observable<Action> {
        return holdSplashSubject.compose(rxSchedulerFactory.applyObservableSchedulers())
    }


}