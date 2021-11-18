package ru.wb.go.ui.unloadingscan

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable
import ru.wb.go.network.exceptions.BadRequestException
import ru.wb.go.network.exceptions.NoInternetException
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.ui.HideBackButtonState
import ru.wb.go.ui.Label
import ru.wb.go.ui.NetworkViewModel
import ru.wb.go.ui.SingleLiveEvent
import ru.wb.go.ui.dialogs.DialogInfoStyle
import ru.wb.go.ui.dialogs.NavigateToInformation
import ru.wb.go.ui.scanner.domain.ScannerState
import ru.wb.go.ui.unloadingscan.domain.ScanProgressData
import ru.wb.go.ui.unloadingscan.domain.UnloadingAction
import ru.wb.go.ui.unloadingscan.domain.UnloadingData
import ru.wb.go.ui.unloadingscan.domain.UnloadingInteractor
import ru.wb.go.utils.LogUtils
import java.util.concurrent.TimeUnit

class UnloadingScanViewModel(
    private val parameters: UnloadingScanParameters,
    compositeDisposable: CompositeDisposable,
    private val resourceProvider: UnloadingScanResourceProvider,
    private val interactor: UnloadingInteractor,
) : NetworkViewModel(compositeDisposable) {

    private val _toolbarBackState = MutableLiveData<HideBackButtonState>()
    val toolbarBackState: LiveData<HideBackButtonState>
        get() = _toolbarBackState

    private val _toolbarLabelState = MutableLiveData<Label>()
    val toolbarLabelState: LiveData<Label>
        get() = _toolbarLabelState

    private val _toolbarNetworkState = MutableLiveData<NetworkState>()
    val toolbarNetworkState: LiveData<NetworkState>
        get() = _toolbarNetworkState

    private val _versionApp = MutableLiveData<String>()
    val versionApp: LiveData<String>
        get() = _versionApp

    private val _soundEvent =
        SingleLiveEvent<UnloadingScanSoundEvent>()
    val soundEvent: LiveData<UnloadingScanSoundEvent>
        get() = _soundEvent

    private val _progressEvent =
        SingleLiveEvent<UnloadingScanProgress>()
    val progressEvent: LiveData<UnloadingScanProgress>
        get() = _progressEvent

    private val _unloadedState =
        MutableLiveData<UnloadingScanBoxState>()
    val unloadedState: LiveData<UnloadingScanBoxState>
        get() = _unloadedState

    private val _returnState =
        MutableLiveData<UnloadingScanReturnState>()
    val returnState: LiveData<UnloadingScanReturnState>
        get() = _returnState

    private val _infoState =
        MutableLiveData<UnloadingScanInfoState>()
    val infoState: LiveData<UnloadingScanInfoState>
        get() = _infoState

    private val _navigationEvent =
        SingleLiveEvent<UnloadingScanNavAction>()
    val navigationEvent: LiveData<UnloadingScanNavAction>
        get() = _navigationEvent

    private val _navigateToMessageInfo = SingleLiveEvent<NavigateToInformation>()
    val navigateToMessageInfo: LiveData<NavigateToInformation>
        get() = _navigateToMessageInfo

    val bottomProgressEvent = MutableLiveData<Boolean>()

    init {
        initTitleToolbar()
        observeNetworkState()
        observeBackButton()
        observeUnloadProcess()
        observeScanProgress()
    }

    private fun observeScanProgress() {
        addSubscription(interactor.scanLoaderProgress()
            .subscribe {
                _progressEvent.value = when (it) {
                    ScanProgressData.Complete -> {
                        interactor.scannerAction(ScannerState.LoaderComplete)
                        UnloadingScanProgress.LoaderComplete
                    }
                    ScanProgressData.Progress -> {
                        interactor.scannerAction(ScannerState.LoaderProgress)
                        UnloadingScanProgress.LoaderProgress
                    }
                }
            })
    }

    private fun observeBackButton() {
        addSubscription(interactor.observeCountUnloadReturnedBoxAndSwitchScreen(parameters.currentOfficeId)
            .subscribe({ _toolbarBackState.value = HideBackButtonState }, {}))
    }

    private fun initTitleToolbar() {
        addSubscription(interactor.officeNameById(parameters.currentOfficeId).subscribe(
            {
                _toolbarLabelState.value = Label(it)
            },
            {
                _toolbarLabelState.value =
                    Label(resourceProvider.getOfficeEmpty(parameters.currentOfficeId))
            }))
    }

    private fun observeUnloadProcess() {
        addSubscription(interactor.observeUnloadingProcess(parameters.currentOfficeId)
            .doOnError { observeScanProcessError(it) }
            .retryWhen { errorObservable -> errorObservable.delay(1, TimeUnit.SECONDS) }
            .subscribe({ observeScanProcessComplete(it) }) { observeScanProcessError(it) })
    }

    private fun observeScanProcessComplete(it: UnloadingData) {
        LogUtils { logDebugApp("observeScanProcessComplete " + it.toString()) }
        val unloadingAccepted =
            resourceProvider.getAccepted(it.flightUnloadedAndUnloadCountEntity.unloadedCount,
                it.flightUnloadedAndUnloadCountEntity.unloadCount)
        val returnAccepted =
            resourceProvider.getAccepted(it.flightTookAndPickupCountEntity.tookCount,
                it.flightTookAndPickupCountEntity.pickupCount)
        when (it.unloadingAction) {
            is UnloadingAction.BoxUnloadAdded -> {
                _soundEvent.value = UnloadingScanSoundEvent.BoxAdded
                _unloadedState.value = UnloadingScanBoxState.Active(unloadingAccepted)
                _returnState.value = UnloadingScanReturnState.Complete(returnAccepted)
                _infoState.value = UnloadingScanInfoState.Unloading(it.unloadingAction.barcode)
            }
            is UnloadingAction.BoxReturnAdded -> {
                _soundEvent.value = UnloadingScanSoundEvent.BoxAdded
                _unloadedState.value = UnloadingScanBoxState.Complete(unloadingAccepted)
                _returnState.value = UnloadingScanReturnState.Active(returnAccepted)
                _infoState.value = UnloadingScanInfoState.Return(it.unloadingAction.barcode)
            }
            UnloadingAction.BoxReturnRemoved -> {
                updateInit(it, unloadingAccepted, returnAccepted)
            }
            is UnloadingAction.BoxDoesNotBelongPvz -> {
                _soundEvent.value = UnloadingScanSoundEvent.BoxSkipAdded
                _navigationEvent.value = navigateToUnloadingBoxNotBelongPvz(it.unloadingAction)
                _unloadedState.value = UnloadingScanBoxState.Error(unloadingAccepted)
                _returnState.value = UnloadingScanReturnState.Complete(returnAccepted)
                _infoState.value = UnloadingScanInfoState.UnloadDeny(it.unloadingAction.barcode)
            }
            is UnloadingAction.BoxWasUnloadedAnotherPvz -> {
                _soundEvent.value = UnloadingScanSoundEvent.BoxSkipAdded
                _navigationEvent.value = navigateToUnloadingBoxNotBelongPvz(it.unloadingAction)
                _unloadedState.value = UnloadingScanBoxState.Error(unloadingAccepted)
                _returnState.value = UnloadingScanReturnState.Complete(returnAccepted)
                _infoState.value = UnloadingScanInfoState.UnloadDeny(it.unloadingAction.barcode)
            }
            is UnloadingAction.BoxInfoEmpty -> {
                _soundEvent.value = UnloadingScanSoundEvent.BoxSkipAdded
                _navigationEvent.value = navigateToUnloadingBoxInfoEmpty(it.unloadingAction)
                _unloadedState.value = UnloadingScanBoxState.Error(unloadingAccepted)
                _returnState.value = UnloadingScanReturnState.Error(returnAccepted)
                _infoState.value = UnloadingScanInfoState.NotInfoDeny(it.unloadingAction.barcode)
            }
            UnloadingAction.Init -> {
                updateInit(it, unloadingAccepted, returnAccepted)
            }
            UnloadingAction.Terminate -> {
//                _unloadedState.value = UnloadingScanBoxState.Complete(unloadingAccepted)
//                _returnState.value = UnloadingScanReturnState.Active(returnAccepted)
//                _infoState.value = UnloadingScanInfoState.Return(
//                    it.flightTookAndPickupCountEntity.barcode ?: "")
            }

        }
    }

    private fun updateInit(
        it: UnloadingData,
        unloadingAccepted: String,
        returnAccepted: String,
    ) {
        val updatedAtUnloading = it.flightUnloadedAndUnloadCountEntity.updatedAt ?: ""
        val updatedAtTook = it.flightTookAndPickupCountEntity.updatedAt ?: ""
        if (updatedAtUnloading.isEmpty() && updatedAtTook.isEmpty()) {
            _unloadedState.value = UnloadingScanBoxState.Complete(unloadingAccepted)
            _returnState.value = UnloadingScanReturnState.Complete(returnAccepted)
            _infoState.value = UnloadingScanInfoState.Empty
        } else {
            if (updatedAtUnloading >= updatedAtTook) {
                _unloadedState.value = UnloadingScanBoxState.Active(unloadingAccepted)
                _returnState.value = UnloadingScanReturnState.Complete(returnAccepted)
                _infoState.value = UnloadingScanInfoState.Unloading(
                    it.flightUnloadedAndUnloadCountEntity.barcode ?: "")
            } else {
                _unloadedState.value = UnloadingScanBoxState.Complete(unloadingAccepted)
                _returnState.value = UnloadingScanReturnState.Active(returnAccepted)
                _infoState.value = UnloadingScanInfoState.Return(
                    it.flightTookAndPickupCountEntity.barcode ?: "")
            }
        }
    }

    private fun navigateToUnloadingBoxNotBelongPvz(unloadingAction: UnloadingAction.BoxDoesNotBelongPvz) =
        UnloadingScanNavAction.NavigateToUnloadingBoxNotBelongPvz(
            resourceProvider.getBoxNotBelongPvzTitle(),
            resourceProvider.getBoxNotBelongPvzDescription(),
            unloadingAction.barcode,
            unloadingAction.address)

    private fun navigateToUnloadingBoxNotBelongPvz(unloadingAction: UnloadingAction.BoxWasUnloadedAnotherPvz) =
        UnloadingScanNavAction.NavigateToUnloadingBoxNotBelongPvz(
            resourceProvider.getBoxNotBelongPvzTitle(),
            resourceProvider.getBoxNotBelongPvzDescription(),
            unloadingAction.barcode,
            unloadingAction.address)

    private fun navigateToUnloadingBoxInfoEmpty(unloadingAction: UnloadingAction.BoxInfoEmpty) =
        UnloadingScanNavAction.NavigateToUnloadingBoxNotBelongPvz(
            resourceProvider.getBoxNotBelongInfoTitle(),
            resourceProvider.getBoxEmptyInfoDescription(),
            unloadingAction.barcode,
            resourceProvider.getBoxNotInfoAddress())

    private fun observeScanProcessError(throwable: Throwable) {
        val message = when (throwable) {
            is NoInternetException -> throwable.message
            is BadRequestException -> throwable.error.message
            else -> resourceProvider.getScanDialogMessage()
        }
        onStopScanner()
        _navigateToMessageInfo.value = NavigateToInformation(
            DialogInfoStyle.ERROR.ordinal,
            resourceProvider.getScanDialogTitle(), message, resourceProvider.getScanDialogButton())
    }

    fun onBoxHandleInput(barcode: String) {
        interactor.barcodeManualInput(barcode.replace("-", ""))
    }

    fun onUnloadingListClicked() {
        _navigationEvent.value =
            UnloadingScanNavAction.NavigateToUploadedBoxes(parameters.currentOfficeId)
    }

    fun onReturnListClicked() {
        _navigationEvent.value =
            UnloadingScanNavAction.NavigateToReturnBoxes(parameters.currentOfficeId)
    }

    fun onaHandleClicked() {
        _navigationEvent.value =
            UnloadingScanNavAction.NavigateToHandleInput(parameters.currentOfficeId)
    }

    fun onCompleteClicked() {
        addSubscription(interactor.isUnloadingComplete(parameters.currentOfficeId)
            .map {
                if (it) UnloadingScanNavAction.NavigateToDelivery
                else UnloadingScanNavAction.NavigateToForcedTermination(parameters.currentOfficeId)
            }
            .subscribe({ _navigationEvent.value = it }, {}))
    }

    fun onStopScanner() {
        interactor.scannerAction(ScannerState.Stop)
    }

    fun onStartScanner() {
        interactor.scannerAction(ScannerState.Start)
    }

    private fun observeNetworkState() {
        addSubscription(interactor.observeNetworkConnected()
            .subscribe({ _toolbarNetworkState.value = it }, {}))
    }

}