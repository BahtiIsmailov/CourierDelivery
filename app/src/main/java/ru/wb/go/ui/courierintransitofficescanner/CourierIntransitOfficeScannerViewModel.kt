package ru.wb.go.ui.courierintransitofficescanner

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.ui.NetworkViewModel
import ru.wb.go.ui.SingleLiveEvent
import ru.wb.go.ui.courierintransitofficescanner.domain.CourierIntransitOfficeScanData
import ru.wb.go.ui.courierintransitofficescanner.domain.CourierIntransitOfficeScannerInteractor
import ru.wb.go.ui.dialogs.NavigateToDialogConfirmInfo
import ru.wb.go.ui.scanner.domain.ScannerState
import ru.wb.go.utils.WaitLoader
import ru.wb.go.utils.analytics.YandexMetricManager
import ru.wb.go.utils.managers.DeviceManager
import ru.wb.go.utils.managers.ErrorDialogData
import ru.wb.go.utils.managers.ErrorDialogManager
import ru.wb.go.utils.managers.PlayManager

class CourierIntransitOfficeScannerViewModel(
    compositeDisposable: CompositeDisposable,
    metric: YandexMetricManager,
    private val interactor: CourierIntransitOfficeScannerInteractor,
    private val resourceProvider: CourierIntransitOfficeScannerResourceProvider,
    private val errorDialogManager: ErrorDialogManager,
    private val playManager: PlayManager,
    private val deviceManager: DeviceManager,
) : NetworkViewModel(compositeDisposable, metric) {

    private val _toolbarNetworkState = MutableLiveData<NetworkState>()
    val toolbarNetworkState: LiveData<NetworkState>
        get() = _toolbarNetworkState

    private val _versionApp = MutableLiveData<String>()
    val versionApp: LiveData<String>
        get() = _versionApp

    private val _toolbarLabelState = MutableLiveData<Label>()
    val toolbarLabelState: LiveData<Label>
        get() = _toolbarLabelState

    private val _navigateToErrorDialog = SingleLiveEvent<ErrorDialogData>()
    val navigateToErrorDialog: LiveData<ErrorDialogData>
        get() = _navigateToErrorDialog

    private val _navigateToDialogConfirmInfo = SingleLiveEvent<NavigateToDialogConfirmInfo>()
    val navigateToDialogConfirmInfo: LiveData<NavigateToDialogConfirmInfo>
        get() = _navigateToDialogConfirmInfo

    private val _navigationState = SingleLiveEvent<CourierIntransitOfficeScannerNavigationState>()
    val officeScannerNavigationState: LiveData<CourierIntransitOfficeScannerNavigationState>
        get() = _navigationState

    private val _beepEvent =
        SingleLiveEvent<CourierIntransitOfficeScannerBeepState>()
    val scannerBeepEvent: LiveData<CourierIntransitOfficeScannerBeepState>
        get() = _beepEvent

    private val _waitLoader =
        SingleLiveEvent<WaitLoader>()
    val waitLoader: LiveData<WaitLoader>
        get() = _waitLoader

    init {
        observeNetworkState()
        fetchVersionApp()
        initTitle()
        initScanner()
    }

    private fun observeNetworkState() {
        addSubscription(
            interactor.observeNetworkConnected()
                .subscribe({ _toolbarNetworkState.value = it }, {})
        )
    }

    private fun fetchVersionApp() {
        _versionApp.value = resourceProvider.getVersionApp(deviceManager.toolbarVersion)
    }

    private fun initTitle() {
        _toolbarLabelState.value = Label(resourceProvider.getLabel())
    }

    private fun initScanner() {
        addSubscription(
            interactor.observeOfficeIdScanProcess()
                .subscribe(
                    { observeOfficeIdScanProcessComplete(it) },
                    { observeOfficeIdScanProcessError(it) })
        )
    }

    private fun observeOfficeIdScanProcessComplete(it: CourierIntransitOfficeScanData) {
        when (it) {
            is CourierIntransitOfficeScanData.NecessaryOfficeScan -> {
                _beepEvent.value = CourierIntransitOfficeScannerBeepState.Office
                _navigationState.value =
                    CourierIntransitOfficeScannerNavigationState.NavigateToUnloadingScanner(
                        it.id
                    )
                onCleared()
            }
            CourierIntransitOfficeScanData.UnknownQrOfficeScan -> {
                _beepEvent.value = CourierIntransitOfficeScannerBeepState.UnknownQrOffice
                _navigationState.value =CourierIntransitOfficeScannerNavigationState.NavigateToOfficeFailed(
                    "QR код офиса не распознан", "Повторите сканирование"
                )
            }
            CourierIntransitOfficeScanData.WrongOfficeScan -> {
                _beepEvent.value = CourierIntransitOfficeScannerBeepState.WrongOffice
                _navigationState.value =CourierIntransitOfficeScannerNavigationState.NavigateToOfficeFailed(
                    "Офис не принадлежит маршруту", "Повторите сканирование"
                )
            }
        }
    }

    private fun observeOfficeIdScanProcessError(it: Throwable) {
        onTechErrorLog("observeOfficeIdScanProcess", it)
        errorDialogManager.showErrorDialog(it, _navigateToErrorDialog)
    }

    fun onCloseScannerClick() {
        onStopScanner()
        _navigationState.value = CourierIntransitOfficeScannerNavigationState.NavigateToMap
    }

    fun onAccessiblyClick() {
        onStartScanner()
        _navigationState.value = CourierIntransitOfficeScannerNavigationState.NavigateToScanner
    }

    fun onErrorDialogConfirmClick() {
        onStartScanner()
    }

    private fun onStopScanner() {
        interactor.scannerAction(ScannerState.StopScan)
    }

    private fun onStartScanner() {
        interactor.scannerAction(ScannerState.Start)
    }

    fun play(resId: Int) {
        playManager.play(resId)
    }

    data class Label(val label: String)

    override fun getScreenTag(): String {
        return SCREEN_TAG
    }

    companion object {
        const val SCREEN_TAG = "CourierIntransitOfficeScanner"
    }

}