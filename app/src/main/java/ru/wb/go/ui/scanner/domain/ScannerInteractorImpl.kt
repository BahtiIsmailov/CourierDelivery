package ru.wb.go.ui.scanner.domain

import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Action
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import ru.wb.go.app.AppPreffsKeys
import ru.wb.go.network.rx.RxSchedulerFactory
import ru.wb.go.utils.managers.SettingsManager
import java.util.concurrent.TimeUnit


class ScannerInteractorImpl(
    private val scannerRepository: ScannerRepository,
    private val settingsManager: SettingsManager,
) : ScannerInteractor {

    private val holdSplashSubject = MutableSharedFlow<Action>(
        extraBufferCapacity = Int.MAX_VALUE, onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    private val prolongHoldSubject = MutableSharedFlow<Action>(
        extraBufferCapacity = Int.MAX_VALUE, onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    private var coroutineScope:CoroutineScope? = null
    private var holdSplashDisposable:Job? = null

    companion object{
        const val HOLD_SCANNER_DELAY = 25000L
    }

    init {
        startTimer()
    }

    override fun barcodeScanned(barcode: String) {
         startTimer()
         scannerRepository.scannerAction(ScannerAction.ScanResult(barcode))
    }

    override fun holdSplashUnlock() {
         scannerRepository.scannerAction(ScannerAction.HoldSplashUnlock)
    }

    override fun prolongHoldTimer() {
        startTimer()
        prolongHoldSubject.tryEmit(Action { })
    }

    override fun observeScannerState(): Flow<ScannerState>  {
        return scannerRepository.observeScannerState()
            .onEach {
                  workWithScan(it)
            }
        }

    private fun workWithScan(it:ScannerState){
        if (it is ScannerState.StartScan) {
            startTimer()
        }
        else if (
            it is ScannerState.StopScan ||
            it is ScannerState.HoldScanComplete ||
            it is ScannerState.HoldScanError ||
            it is ScannerState.HoldScanUnknown
        ) {
            stopTimer()
        }
    }

    private fun startTimer() {
        if (!settingsManager.getSetting(AppPreffsKeys.SETTING_SANNER_OFF, false)) {
            return
        }
        if (coroutineScope == null) {
            coroutineScope = CoroutineScope(SupervisorJob())
            prolongHoldSubject
            .onEach {
                delay(HOLD_SCANNER_DELAY,)
                scannerRepository.scannerAction(ScannerAction.HoldSplashLock)
                holdSplashSubject.tryEmit(Action { })
            }
            .launchIn(coroutineScope!!)

        }
    }

//    private fun startTimer() {
//        if (!settingsManager.getSetting(AppPreffsKeys.SETTING_SANNER_OFF, false)) {
//            return
//        }
//        if (holdSplashDisposable == null) {
//            holdSplashDisposable = Observable.timer(HOLD_SCANNER_DELAY, TimeUnit.SECONDS)
//                .repeatWhen { repeatHandler -> repeatHandler.flatMap { prolongHoldSubject } }
//                .subscribe({
//                    scannerRepository.scannerAction(ScannerAction.HoldSplashLock)
//                    holdSplashSubject.onNext(Action { })
//                }, {})
//        }
//    }
     private fun stopTimer() {
        coroutineScope?.cancel()
        coroutineScope = null
    }
//    private fun stopTimer() {
//        if (holdSplashDisposable != null) {
//            holdSplashDisposable!!.dispose()
//            holdSplashDisposable = null
//        }
//    }
    override fun observeHoldSplash(): Flow<Action> {
        return holdSplashSubject
    }


}
