package ru.wb.go.ui.scanner.domain

import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Action
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
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

    private var holdSplashDisposable: Disposable? = null

    companion object{

        const val HOLD_SCANNER_DELAY = 25L
    }

    init {

    }

    override fun barcodeScanned(barcode: String) {
         startTimer()
         scannerRepository.scannerAction(ScannerAction.ScanResult(barcode))
    }



    override suspend fun holdSplashUnlock() {
         scannerRepository.scannerAction(ScannerAction.HoldSplashUnlock)
    }



    override  fun prolongHoldTimer() {
        startTimer()
        prolongHoldSubject.tryEmit(Action { })
    }

    override fun observeScannerState(): Flow<ScannerState>  {
        return scannerRepository.observeScannerState().onEach {
                  workWithScan(it)
              }
                .flowOn(Dispatchers.IO)
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
        if (holdSplashDisposable == null) {
            Observable.timer(HOLD_SCANNER_DELAY, TimeUnit.SECONDS)
            scannerRepository.scannerAction(ScannerAction.HoldSplashLock)
            holdSplashSubject.tryEmit(Action { })

        }
    }
     private fun stopTimer() {
        if (holdSplashDisposable != null) {
            holdSplashDisposable!!.dispose()
            holdSplashDisposable = null
        }
    }

    override suspend fun observeHoldSplash(): Flow<Action> {
        return withContext(Dispatchers.IO){
            holdSplashSubject
        }
    }


}

/*
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
                    scannerRepository.scannerAction(ScannerAction.HoldSplashLock)
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

    companion object {
        const val HOLD_SCANNER_DELAY = 25L
    }

 */