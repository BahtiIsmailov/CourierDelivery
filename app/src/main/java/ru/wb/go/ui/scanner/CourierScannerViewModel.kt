package ru.wb.go.ui.scanner

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import ru.wb.go.app.AppPreffsKeys
import ru.wb.go.ui.NetworkViewModel
import ru.wb.go.ui.SingleLiveEvent
import ru.wb.go.ui.scanner.domain.ScannerInteractor
import ru.wb.go.ui.scanner.domain.ScannerState
import ru.wb.go.utils.analytics.YandexMetricManager
import ru.wb.go.utils.managers.SettingsManager

class CourierScannerViewModel(
    compositeDisposable: CompositeDisposable,
    metric: YandexMetricManager,
    private val interactor: ScannerInteractor,
    private val settingsManager: SettingsManager,
) : NetworkViewModel(compositeDisposable, metric) {

//    private val _scannerAction = SingleLiveEvent<ScannerState>()
//    val scannerAction: LiveData<ScannerState>
//        get() = _scannerAction

    private val _scannerAction = Channel<ScannerState>(capacity = Int.MAX_VALUE)
    val scannerAction: Flow<ScannerState>
        get() = _scannerAction.receiveAsFlow()

    private val _flashState = SingleLiveEvent<Boolean>()
    val flashState: LiveData<Boolean>
        get() = _flashState

    fun update() {
        observeHoldSplash()
        Log.e("ScannerTag","observeHoldSplash")
        flashState()
        Log.e("ScannerTag","flashState")
        observeScannerState()
        Log.e("ScannerTag","observeScannerState")
    }

    private fun observeScannerState() {
        interactor.observeScannerState()
            .onEach {
                Log.e("ScannerTag","observeScannerState")
                _scannerAction.trySend(it)
            }
            .launchIn(viewModelScope)
    }

    private fun flashState() {
        _flashState.value =
            settingsManager.getSetting(
                AppPreffsKeys.SETTING_START_FLASH_ON,
                false
            )

    }

    private fun observeHoldSplash() {
        interactor.observeHoldSplash()
            .onEach {
                Log.e("ScannerTag","observeHoldSplash1 $it")
                _scannerAction.trySend(ScannerState.StopScanWithHoldSplash)
            }
            .launchIn(viewModelScope)
    }

    fun onBarcodeScanned(barcode: String) {
        interactor.prolongHoldTimer()
        interactor.barcodeScanned(barcode) //пришел баркот номер 1 1 1 135223
    }

    fun onHoldSplashClick() {
        interactor.prolongHoldTimer()
        interactor.holdSplashUnlock()
        _scannerAction.trySend(ScannerState.StartScan)
    }

    fun onDestroy() {
        clearSubscription()
        Log.e("ScannerTag","clearSubscription")// backStack
    }

    fun switchFlashlight() {
        val state = !_flashState.value!!
        _flashState.value = state
    }

    override fun getScreenTag(): String {
        return SCREEN_TAG
    }

    companion object {
        const val SCREEN_TAG = "Scanner"
    }

}

/*
  private val _scannerAction = SingleLiveEvent<ScannerState>()
    val scannerAction: LiveData<ScannerState>
        get() = _scannerAction

    private val _flashState = SingleLiveEvent<Boolean>()
    val flashState: LiveData<Boolean>
        get() = _flashState

    fun update() {
        observeHoldSplash()
        flashState()
        observeScannerState()
    }

    private fun observeScannerState() {
        addSubscription(
            interactor.observeScannerState()
                .subscribe { _scannerAction.value = it }
        )
    }

    private fun flashState() {
        _flashState.value = settingsManager.getSetting(AppPreffsKeys.SETTING_START_FLASH_ON, false)
    }

    private fun observeHoldSplash() {
        addSubscription(interactor.observeHoldSplash().subscribe({
            _scannerAction.value = ScannerState.StopScanWithHoldSplash
        }, {}))
    }

    fun onBarcodeScanned(barcode: String) {
        interactor.prolongHoldTimer()
        interactor.barcodeScanned(barcode)
    }

    fun onHoldSplashClick() {
        interactor.prolongHoldTimer()
        interactor.holdSplashUnlock()
        _scannerAction.value = ScannerState.StartScan
    }

    fun onDestroy() {
        clearSubscription()
    }

    fun switchFlashlight() {
        val state = !_flashState.value!!
        _flashState.postValue(state)
    }

    override fun getScreenTag(): String {
        return SCREEN_TAG
    }

    companion object {
        const val SCREEN_TAG = "Scanner"
    }


 */