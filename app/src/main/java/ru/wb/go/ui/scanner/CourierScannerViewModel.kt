package ru.wb.go.ui.scanner

import androidx.lifecycle.LiveData
import io.reactivex.disposables.CompositeDisposable
import ru.wb.go.ui.NetworkViewModel
import ru.wb.go.ui.SingleLiveEvent
import ru.wb.go.ui.scanner.domain.ScannerInteractor
import ru.wb.go.ui.scanner.domain.ScannerState
import ru.wb.go.utils.analytics.YandexMetricManager

class CourierScannerViewModel(
    compositeDisposable: CompositeDisposable,
    metric: YandexMetricManager,
    private val interactor: ScannerInteractor,
) : NetworkViewModel(compositeDisposable, metric) {

    private val _scannerAction = SingleLiveEvent<ScannerState>()
    val scannerAction: LiveData<ScannerState>
        get() = _scannerAction

    init {
        addSubscription(interactor.observeScannerState()
            .subscribe { _scannerAction.value = it }
        )
    }

    fun onBarcodeScanned(barcode: String) {
        interactor.barcodeScanned(barcode)
    }

    override fun getScreenTag(): String {
        return SCREEN_TAG
    }

    companion object {
        const val SCREEN_TAG = "Scanner"
    }

}