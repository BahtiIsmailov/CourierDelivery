package ru.wb.go.ui.scanner

import androidx.lifecycle.LiveData
import io.reactivex.disposables.CompositeDisposable
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

    init {

        _flashState.value = settingsManager.loadFlash()

        addSubscription(
            interactor.observeScannerState()
                .subscribe { _scannerAction.value = it }
        )
    }

    fun onBarcodeScanned(barcode: String) {
        interactor.barcodeScanned(barcode)
    }

    fun switchFlashlight(){
        val state = !_flashState.value!!
        _flashState.postValue(state)
        settingsManager.saveFlash(state)

    }

    override fun getScreenTag(): String {
        return SCREEN_TAG
    }

    companion object {
        const val SCREEN_TAG = "Scanner"
    }

}