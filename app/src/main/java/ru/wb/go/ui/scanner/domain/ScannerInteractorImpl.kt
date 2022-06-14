package ru.wb.go.ui.scanner.domain

import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Action
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.onEach
import ru.wb.go.app.AppPreffsKeys
import ru.wb.go.network.rx.RxSchedulerFactory
import ru.wb.go.utils.managers.SettingsManager
import java.util.concurrent.TimeUnit


class ScannerInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val scannerRepository: ScannerRepository,
    private val settingsManager: SettingsManager,
) : ScannerInteractor {

    private val holdSplashSubject = MutableSharedFlow<Action>()
    private val prolongHoldSubject = MutableSharedFlow<Action>()

    private var holdSplashDisposable: Disposable? = null

    companion object{

        const val HOLD_SCANNER_DELAY = 25L
    }

    init {

    }

    override suspend fun barcodeScanned(barcode: String) {
         startTimer()
         scannerRepository.scannerAction(ScannerAction.ScanResult(barcode))
    }



    override suspend fun holdSplashUnlock() {
         scannerRepository.scannerAction(ScannerAction.HoldSplashUnlock)
    }



    override suspend fun prolongHoldTimer() {
        startTimer()
        prolongHoldSubject.emit(Action { })
    }

    override suspend fun observeScannerState(): Flow<ScannerState>  {
          return withContext(Dispatchers.IO){
              scannerRepository.observeScannerState().onEach {
                  workWithScan(it)
              }
              scannerRepository.observeScannerState()
        }
    }

    private suspend fun workWithScan(it:ScannerState){
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

    private suspend fun startTimer() {
        if (!settingsManager.getSetting(AppPreffsKeys.SETTING_SANNER_OFF, false)) {
            return
        }
        if (holdSplashDisposable == null) {
            Observable.timer(HOLD_SCANNER_DELAY, TimeUnit.SECONDS)
            scannerRepository.scannerAction(ScannerAction.HoldSplashLock)
            holdSplashSubject.emit(Action { })

        }
    }
//    private fun startTimer() {
//        if (!settingsManager.getSetting(AppPreffsKeys.SETTING_SANNER_OFF, false)) {
//            return
//        }
//        if (holdSplashDisposable == null) {
//            holdSplashDisposable = Observable.timer(HOLD_SCANNER_DELAY, TimeUnit.SECONDS)
//                 repeatHandler -> repeatHandler.flatMap { prolongHoldSubject }
//                .subscribe({
//                    scannerRepository.scannerAction(ScannerAction.HoldSplashLock)
//                    holdSplashSubject.onNext(Action { })
//                }, {})
//        }
//    }

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