package ru.wb.go.ui.scanner

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.wb.go.app.AppPreffsKeys
import ru.wb.go.ui.NetworkViewModel
import ru.wb.go.ui.SingleLiveEvent
import ru.wb.go.ui.scanner.domain.ScannerInteractor
import ru.wb.go.ui.scanner.domain.ScannerState
import ru.wb.go.utils.managers.SettingsManager

class CourierScannerViewModel(
    private val interactor: ScannerInteractor,
    private val settingsManager: SettingsManager,
) : NetworkViewModel( ) {

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
        interactor.observeScannerState()
            .onEach {
                _scannerAction.value = it
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
                _scannerAction.value = ScannerState.StopScanWithHoldSplash
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
        _scannerAction.value = ScannerState.StartScan
    }

    fun onDestroy() {
        viewModelScope.coroutineContext.cancelChildren()
        //clearSubscription() // backStack
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

