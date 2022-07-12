package ru.wb.go.ui.courierintransitofficescanner

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.wb.go.ui.ServicesViewModel
import ru.wb.go.ui.SingleLiveEvent
import ru.wb.go.ui.courierintransitofficescanner.domain.CourierIntransitOfficeScanData
import ru.wb.go.ui.courierintransitofficescanner.domain.CourierIntransitOfficeScannerInteractor
import ru.wb.go.ui.dialogs.NavigateToDialogConfirmInfo
import ru.wb.go.ui.scanner.domain.ScannerState
import ru.wb.go.utils.WaitLoader
import ru.wb.go.utils.managers.ErrorDialogData
import ru.wb.go.utils.managers.ErrorDialogManager
import ru.wb.go.utils.managers.PlayManager

class CourierIntransitOfficeScannerViewModel(
    private val interactor: CourierIntransitOfficeScannerInteractor,
    private val resourceProvider: CourierIntransitOfficeScannerResourceProvider,
    private val errorDialogManager: ErrorDialogManager,
    private val playManager: PlayManager,
) : ServicesViewModel(interactor, resourceProvider) {

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
         interactor.observeOfficeIdScanProcess()
             .onEach {
                 observeOfficeIdScanProcessComplete(it)
             }
             .catch {
                 logException(it,"initScanner")
                 observeOfficeIdScanProcessError(it)
             }
             .launchIn(viewModelScope)
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
            is CourierIntransitOfficeScanData.UnknownQrOfficeScan -> {
                onStopScanner()
                _beepEvent.value = CourierIntransitOfficeScannerBeepState.UnknownQrOffice
                _navigationState.value =
                    CourierIntransitOfficeScannerNavigationState.NavigateToOfficeFailed(
                        "QR-код ПВЗ не распознан", "QR код должен предоставить менеджер ПВЗ"
                    )
            }
            is CourierIntransitOfficeScanData.WrongOfficeScan -> {
                onStopScanner()
                _beepEvent.value = CourierIntransitOfficeScannerBeepState.WrongOffice
                _navigationState.value =
                    CourierIntransitOfficeScannerNavigationState.NavigateToOfficeFailed(
                        "У вас нет коробок для этого ПВЗ.", "Проверьте свой маршрут"
                    )
            }
            is CourierIntransitOfficeScanData.HoldSplashUnlock -> _infoCameraVisibleState.value = true
            is CourierIntransitOfficeScanData.HoldSplashLock -> _infoCameraVisibleState.value = false
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
        //viewModelScope.coroutineContext.cancelChildren()
        //clearSubscription()
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

}

