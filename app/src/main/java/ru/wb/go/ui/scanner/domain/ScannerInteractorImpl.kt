package ru.wb.go.ui.scanner.domain

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import ru.wb.go.app.AppPreffsKeys
import ru.wb.go.utils.managers.SettingsManager


class ScannerInteractorImpl(
    private val scannerRepository: ScannerRepository,
    private val settingsManager: SettingsManager,
) : ScannerInteractor {

    private val holdSplashSubject = MutableSharedFlow<Unit>(
        extraBufferCapacity = Int.MAX_VALUE, onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
//    private val prolongHoldSubject = MutableSharedFlow<Unit>(
//        extraBufferCapacity = Int.MAX_VALUE, onBufferOverflow = BufferOverflow.DROP_OLDEST
//    )

//    private val holdSplashSubject = MutableStateFlow(Unit)
    private val prolongHoldSubject = MutableStateFlow(Unit)

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
        //prolongHoldSubject.tryEmit(Unit)
        prolongHoldSubject.update {  }
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
                //delay(HOLD_SCANNER_DELAY,)
                scannerRepository.scannerAction(ScannerAction.HoldSplashLock)
                holdSplashSubject.tryEmit(Unit)
                //holdSplashSubject.update { }
            }
            .launchIn(coroutineScope!!)

        }
    }

      private fun stopTimer() {
        coroutineScope?.cancel()
        coroutineScope = null
    }
     override fun observeHoldSplash(): Flow<Unit> {
        return holdSplashSubject
    }


}
