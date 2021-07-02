package com.wb.logistics.ui.unloading

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.wb.logistics.network.exceptions.BadRequestException
import com.wb.logistics.network.exceptions.NoInternetException
import com.wb.logistics.ui.NetworkViewModel
import com.wb.logistics.ui.SingleLiveEvent
import com.wb.logistics.ui.scanner.domain.ScannerAction
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

//    private val _messageEvent =
//        SingleLiveEvent<UnloadingScanMessageEvent>()
//    val toastEvent: LiveData<UnloadingScanMessageEvent>
//        get() = _messageEvent

    private val _soundEvent =
        SingleLiveEvent<UnloadingScanSoundEvent>()
    val soundEvent: LiveData<UnloadingScanSoundEvent>
        get() = _soundEvent

    private val _unloadedState =
        MutableLiveData<UnloadingScanBoxState>()
    val unloadedState: LiveData<UnloadingScanBoxState>
        get() = _unloadedState

    private val _returnState =
        MutableLiveData<UnloadingReturnState>()
    val returnState: LiveData<UnloadingReturnState>
        get() = _returnState

    private val _navigationEvent =
        SingleLiveEvent<UnloadingScanNavAction>()
    val navigationEvent: LiveData<UnloadingScanNavAction>
        get() = _navigationEvent

    private val _navigateToMessageInfo = SingleLiveEvent<NavigateToMessageInfo>()
    val navigateToMessageInfo: LiveData<NavigateToMessageInfo>
        get() = _navigateToMessageInfo

    val bottomProgressEvent = MutableLiveData<Boolean>()

    init {
        // TODO: 01.07.2021 восстанавливать реактивный поток сканирования после ошибки
        initTitleToolbar()
        observeBackButton()
        observeScanProcess()
        observeUnloadedBoxes()
        observeReturnBoxes()
    }

    private fun observeBackButton() {
        addSubscription(interactor.observeCountUnloadReturnedBoxAndSwitchScreen(parameters.dstOfficeId)
            .subscribe({ _toolbarBackState.value = HideBackButtonState }, {}))
    }

    private fun initTitleToolbar() {
        addSubscription(interactor.officeNameById(parameters.dstOfficeId).subscribe(
            {
                _toolbarLabelState.value = Label(it)
            },
            {
                _toolbarLabelState.value =
                    Label(resourceProvider.getOfficeEmpty(parameters.dstOfficeId))
            }))
    }

    private fun observeScanProcess() {
        addSubscription(interactor.observeScanProcess(parameters.dstOfficeId)
            .doOnError { observeScanProcessError(it) }
            .retryWhen { errorObservable -> errorObservable.delay(1, TimeUnit.SECONDS) }
            .subscribe({ observeScanProcessComplete(it) }) { observeScanProcessError(it) })
    }

    private fun observeScanProcessComplete(it: UnloadingData) {
        LogUtils {logDebugApp("observeScanProcessComplete " + it)}
        when (it) {
            is UnloadingData.BoxAlreadyUnloaded -> {
//                _messageEvent.value =
//                    UnloadingScanMessageEvent.BoxDelivery(
//                        resourceProvider.getAlreadyDelivery(it.barcode))
                _soundEvent.value = UnloadingScanSoundEvent.BoxSkipAdded
            }

            is UnloadingData.BoxAlreadyReturn -> {
//                _messageEvent.value =
//                    UnloadingScanMessageEvent.BoxReturned(
//                        resourceProvider.getAlreadyReturned(it.barcode))
                _soundEvent.value = UnloadingScanSoundEvent.BoxSkipAdded
            }

            is UnloadingData.BoxUnloadAdded -> {
//                _messageEvent.value =
//                    UnloadingScanMessageEvent.BoxDelivery(resourceProvider.getDelivered(it.barcode))
                _soundEvent.value = UnloadingScanSoundEvent.BoxAdded
            }

            is UnloadingData.BoxReturnAdded -> {
//                _messageEvent.value =
//                    UnloadingScanMessageEvent.BoxReturned(resourceProvider.getReturned(it.barcode))
                _soundEvent.value = UnloadingScanSoundEvent.BoxAdded
            }

            is UnloadingData.BoxDoesNotBelongPvz -> {
                LogUtils {logDebugApp("UnloadingData.BoxDoesNotBelongPvz " + it)}
                _navigationEvent.value =
                    UnloadingScanNavAction.NavigateToUnloadingBoxNotBelongPvz(
                        resourceProvider.getBoxNotBelongPvzTitle(),
                        resourceProvider.getBoxNotBelongPvzDescription(),
                        it.barcode,
                        it.address)
                _soundEvent.value = UnloadingScanSoundEvent.BoxSkipAdded
            }
            is UnloadingData.BoxEmptyInfo -> {
                _soundEvent.value = UnloadingScanSoundEvent.BoxSkipAdded
                _navigationEvent.value =
                    UnloadingScanNavAction.NavigateToUnloadingBoxNotBelongPvz(
                        resourceProvider.getBoxNotBelongInfoTitle(),
                        resourceProvider.getBoxEmptyInfoDescription(),
                        it.barcode,
                        resourceProvider.getBoxNotBelongAddress())
            }
        }
    }

    private fun observeScanProcessError(throwable: Throwable) {
        val message = when (throwable) {
            is NoInternetException -> throwable.message
            is BadRequestException -> throwable.error.message
            else -> resourceProvider.getScanDialogMessage()
        }
        _navigateToMessageInfo.value = NavigateToMessageInfo(
            resourceProvider.getScanDialogTitle(), message, resourceProvider.getScanDialogButton())
    }

    private fun observeUnloadedBoxes() {
        addSubscription(interactor.observeUnloadedAndAttachedBoxes(parameters.dstOfficeId)
            .subscribe({
                val uploadedList = it.first
                val attachedList = it.second
                val accepted =
                    "" + uploadedList.size + "/" + (attachedList.size + uploadedList.size)

                if (uploadedList.isEmpty() && attachedList.isEmpty()) {
                    _unloadedState.value =
                        UnloadingScanBoxState.UnloadedBoxesEmpty(accepted)
                    return@subscribe
                }
                if (uploadedList.isEmpty()) {
                    _unloadedState.value =
                        UnloadingScanBoxState.UnloadedBoxesComplete(accepted)
                    return@subscribe
                }
                // TODO: 28.04.2021 выключено до реализации события завершения выгрузки

                _unloadedState.value =
                    UnloadingScanBoxState.UnloadedBoxesActive(accepted, uploadedList.last().barcode)
            }, {
                LogUtils { logDebugApp(it.toString()) }
            }))
    }

    private fun observeReturnBoxes() {
        addSubscription(interactor.observeReturnBoxes(parameters.dstOfficeId).subscribe {
            _returnState.value =
                if (it.isEmpty()) UnloadingReturnState.ReturnBoxesEmpty("0")
                else
                    UnloadingReturnState.ReturnBoxesComplete(it.size.toString(), it.last().barcode)
        })
    }

    fun onBoxHandleInput(barcode: String) {
        interactor.barcodeManualInput(barcode.replace("-", ""))
    }

    fun onUnloadingListClicked() {
        _navigationEvent.value =
            UnloadingScanNavAction.NavigateToUploadedBoxes(parameters.dstOfficeId)
    }

    fun onReturnListClicked() {
        _navigationEvent.value =
            UnloadingScanNavAction.NavigateToReturnBoxes(parameters.dstOfficeId)
    }

    fun onaHandleClicked() {
        _navigationEvent.value =
            UnloadingScanNavAction.NavigateToHandleInput(parameters.dstOfficeId)
    }

    fun onCompleteClicked() {
        addSubscription(interactor.observeAttachedBoxes(parameters.dstOfficeId)
            .subscribe({
                if (it.isEmpty()) {
                    addSubscription(interactor.completeUnloading().subscribe {
                        _navigationEvent.value = UnloadingScanNavAction.NavigateToBack
                    })
                } else _navigationEvent.value =
                    UnloadingScanNavAction.NavigateToForcedTermination(parameters.dstOfficeId)
            }, {})
        )
    }

    fun onStopScanner() {
        interactor.scannerAction(ScannerAction.Stop)
    }

    object HideBackButtonState

    data class Label(val label: String)

    data class NavigateToMessageInfo(val title: String, val message: String, val button: String)

}