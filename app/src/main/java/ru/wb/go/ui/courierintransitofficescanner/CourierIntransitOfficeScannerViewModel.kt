package ru.wb.go.ui.courierintransitofficescanner

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ru.wb.go.ui.ServicesViewModel
import ru.wb.go.ui.SingleLiveEvent
import ru.wb.go.ui.courierintransitofficescanner.domain.CourierIntransitOfficeScanData
import ru.wb.go.ui.courierintransitofficescanner.domain.CourierIntransitOfficeScannerInteractor
import ru.wb.go.ui.dialogs.NavigateToDialogConfirmInfo
import ru.wb.go.ui.scanner.domain.ScannerState
import ru.wb.go.utils.WaitLoader
import ru.wb.go.utils.analytics.YandexMetricManager
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
) : ServicesViewModel(compositeDisposable, metric, interactor, resourceProvider) {

    private val _toolbarLabelState = MutableLiveData<String>()
    val toolbarLabelState: LiveData<String>
        get() = _toolbarLabelState

    private val _infoCameraVisibleState = MutableLiveData<Boolean>()
    val infoCameraVisibleState: LiveData<Boolean>
        get() = _infoCameraVisibleState

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
        initTitle()
        initScanner()
    }

    private fun initTitle() {
        _toolbarLabelState.postValue( resourceProvider.getLabel())
    }

    private fun initScanner() {
        viewModelScope.launch{
            try {
                observeOfficeIdScanProcessComplete(interactor.observeOfficeIdScanProcess())
            }catch (e:Exception){
                observeOfficeIdScanProcessError(e)
            }
        }

    }

    private fun observeOfficeIdScanProcessComplete(it: CourierIntransitOfficeScanData) {
        when (it) {
            is CourierIntransitOfficeScanData.NecessaryOfficeScan -> {
                _beepEvent.postValue( CourierIntransitOfficeScannerBeepState.Office)
                _navigationState.postValue(
                    CourierIntransitOfficeScannerNavigationState.NavigateToUnloadingScanner(
                        it.id
                    ))
                onCleared()
            }
            CourierIntransitOfficeScanData.UnknownQrOfficeScan -> {
                onStopScanner()
                _beepEvent.postValue( CourierIntransitOfficeScannerBeepState.UnknownQrOffice)
                _navigationState.postValue(
                    CourierIntransitOfficeScannerNavigationState.NavigateToOfficeFailed(
                        "QR код офиса не распознан", "Повторите сканирование"
                    ))
            }
            CourierIntransitOfficeScanData.WrongOfficeScan -> {
                onStopScanner()
                _beepEvent.postValue( CourierIntransitOfficeScannerBeepState.WrongOffice)
                _navigationState.postValue(
                    CourierIntransitOfficeScannerNavigationState.NavigateToOfficeFailed(
                        "Офис не принадлежит маршруту", "Повторите сканирование"
                    ))
            }
            CourierIntransitOfficeScanData.HoldSplashUnlock -> _infoCameraVisibleState.postValue( true)
            CourierIntransitOfficeScanData.HoldSplashLock -> _infoCameraVisibleState.postValue( false)
        }
    }

    private fun observeOfficeIdScanProcessError(it: Throwable) {
        onTechErrorLog("observeOfficeIdScanProcess", it)
        errorDialogManager.showErrorDialog(it, _navigateToErrorDialog)
    }

    fun onCloseScannerClick() {
        onStopScanner()
        _navigationState.postValue( CourierIntransitOfficeScannerNavigationState.NavigateToMap)
    }

    fun onAccessiblyClick() {
        onStartScanner()
        _navigationState.postValue( CourierIntransitOfficeScannerNavigationState.NavigateToScanner)
    }

    fun onDestroy() {
        clearSubscription()
    }

    fun onErrorDialogConfirmClick() {
        onStartScanner()
    }

    private fun onStopScanner() {
        viewModelScope.launch {
            interactor.scannerAction(ScannerState.StopScan)
        }
    }

    private fun onStartScanner() {
        viewModelScope.launch {
            interactor.scannerAction(ScannerState.StartScan)
        }
    }

    fun play(resId: Int) {
        playManager.play(resId)
    }

    override fun getScreenTag(): String {
        return SCREEN_TAG
    }

    companion object {
        const val SCREEN_TAG = "CourierIntransitOfficeScanner"
    }

}

/*
 private val _toolbarLabelState = MutableLiveData<String>()
    val toolbarLabelState: LiveData<String>
        get() = _toolbarLabelState

    private val _infoCameraVisibleState = MutableLiveData<Boolean>()
    val infoCameraVisibleState: LiveData<Boolean>
        get() = _infoCameraVisibleState

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
        initTitle()
        initScanner()
    }

    private fun initTitle() {
        _toolbarLabelState.value = resourceProvider.getLabel()
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
                onStopScanner()
                _beepEvent.value = CourierIntransitOfficeScannerBeepState.UnknownQrOffice
                _navigationState.value =
                    CourierIntransitOfficeScannerNavigationState.NavigateToOfficeFailed(
                        "QR код офиса не распознан", "Повторите сканирование"
                    )
            }
            CourierIntransitOfficeScanData.WrongOfficeScan -> {
                onStopScanner()
                _beepEvent.value = CourierIntransitOfficeScannerBeepState.WrongOffice
                _navigationState.value =
                    CourierIntransitOfficeScannerNavigationState.NavigateToOfficeFailed(
                        "Офис не принадлежит маршруту", "Повторите сканирование"
                    )
            }
            CourierIntransitOfficeScanData.HoldSplashUnlock -> _infoCameraVisibleState.value = true
            CourierIntransitOfficeScanData.HoldSplashLock -> _infoCameraVisibleState.value = false
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

    fun onDestroy() {
        clearSubscription()
    }

    fun onErrorDialogConfirmClick() {
        onStartScanner()
    }

    private fun onStopScanner() {
        interactor.scannerAction(ScannerState.StopScan)
    }

    private fun onStartScanner() {
        interactor.scannerAction(ScannerState.StartScan)
    }

    fun play(resId: Int) {
        playManager.play(resId)
    }

    override fun getScreenTag(): String {
        return SCREEN_TAG
    }

    companion object {
        const val SCREEN_TAG = "CourierIntransitOfficeScanner"
    }


 */