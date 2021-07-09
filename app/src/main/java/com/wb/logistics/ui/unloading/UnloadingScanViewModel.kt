package com.wb.logistics.ui.unloading

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.wb.logistics.network.exceptions.BadRequestException
import com.wb.logistics.network.exceptions.NoInternetException
import com.wb.logistics.ui.NetworkViewModel
import com.wb.logistics.ui.SingleLiveEvent
import com.wb.logistics.ui.scanner.domain.ScannerAction
import com.wb.logistics.ui.unloading.domain.UnloadingAction
import com.wb.logistics.ui.unloading.domain.UnloadingData
import com.wb.logistics.ui.unloading.domain.UnloadingInteractor
import com.wb.logistics.utils.LogUtils
import io.reactivex.disposables.CompositeDisposable
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

    private val _soundEvent =
        SingleLiveEvent<UnloadingScanSoundEvent>()
    val soundEvent: LiveData<UnloadingScanSoundEvent>
        get() = _soundEvent

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

    private val _navigateToMessageInfo = SingleLiveEvent<NavigateToMessageInfo>()
    val navigateToMessageInfo: LiveData<NavigateToMessageInfo>
        get() = _navigateToMessageInfo

    val bottomProgressEvent = MutableLiveData<Boolean>()

    init {
        initTitleToolbar()
        observeBackButton()
        observeUnloadProcess()
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
        LogUtils { logDebugApp("observeScanProcessComplete " + it) }
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
                _infoState.value = UnloadingScanInfoState.Unloading(it.unloadingAction.barcode)
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
                        _infoState.value =
                            UnloadingScanInfoState.Unloading(it.flightUnloadedAndUnloadCountEntity.barcode
                                ?: "")
                    } else {
                        _unloadedState.value = UnloadingScanBoxState.Complete(unloadingAccepted)
                        _returnState.value = UnloadingScanReturnState.Active(returnAccepted)
                        _infoState.value =
                            UnloadingScanInfoState.Return(it.flightTookAndPickupCountEntity.barcode
                                ?: "")
                    }
                }
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
        LogUtils { logDebugApp("observeScanProcessError " + throwable.toString()) }
        val message = when (throwable) {
            is NoInternetException -> throwable.message
            is BadRequestException -> throwable.error.message
            else -> resourceProvider.getScanDialogMessage()
        }
        _navigateToMessageInfo.value = NavigateToMessageInfo(
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
        interactor.scannerAction(ScannerAction.Stop)
    }

    object HideBackButtonState

    data class Label(val label: String)

    data class NavigateToMessageInfo(val title: String, val message: String, val button: String)

}