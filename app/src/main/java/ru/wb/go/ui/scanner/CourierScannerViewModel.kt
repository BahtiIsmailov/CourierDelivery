package ru.wb.go.ui.scanner

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.launch
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
        viewModelScope.launch {
            _scannerAction.postValue(interactor.observeScannerState())
        }
    }

    private fun flashState() {
        _flashState.postValue( settingsManager.getSetting(AppPreffsKeys.SETTING_START_FLASH_ON, false))
    }

//    private fun observeHoldSplash() {
//        addSubscription(interactor.observeHoldSplash().subscribe({
//            _scannerAction.postValue( ScannerState.StopScanWithHoldSplash)
//        }, {}))
//    }

    private fun observeHoldSplash() {
        viewModelScope.launch {
            interactor.observeHoldSplash()
            _scannerAction.postValue( ScannerState.StopScanWithHoldSplash)
        }
    }

    fun onBarcodeScanned(barcode: String) {
        interactor.prolongHoldTimer()
        interactor.barcodeScanned(barcode) ////////////////ScanerAction
    }

    fun onHoldSplashClick() {
        interactor.prolongHoldTimer()
        interactor.holdSplashUnlock()
        _scannerAction.postValue( ScannerState.StartScan)
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

}