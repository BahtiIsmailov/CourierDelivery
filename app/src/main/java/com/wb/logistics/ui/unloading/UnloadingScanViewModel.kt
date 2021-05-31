package com.wb.logistics.ui.unloading

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.wb.logistics.ui.NetworkViewModel
import com.wb.logistics.ui.SingleLiveEvent
import com.wb.logistics.ui.scanner.domain.ScannerAction
import com.wb.logistics.ui.unloading.domain.UnloadingData
import com.wb.logistics.ui.unloading.domain.UnloadingInteractor
import com.wb.logistics.utils.LogUtils
import com.wb.logistics.utils.managers.ScreenManager
import io.reactivex.disposables.CompositeDisposable

class UnloadingScanViewModel(
    private val parameters: UnloadingScanParameters,
    compositeDisposable: CompositeDisposable,
    private val resourceProvider: UnloadingScanResourceProvider,
    private val interactor: UnloadingInteractor,
    private val screenManager: ScreenManager,
) : NetworkViewModel(compositeDisposable) {

    private val _toolbarBackState = MutableLiveData<BackButtonState>()
    val toolbarBackState: LiveData<BackButtonState>
        get() = _toolbarBackState

    private val _toolbarLabelState = MutableLiveData<Label>()
    val toolbarLabelState: LiveData<Label>
        get() = _toolbarLabelState

    private val _messageEvent =
        SingleLiveEvent<UnloadingScanMessageEvent<Nothing>>()
    val toastEvent: LiveData<UnloadingScanMessageEvent<Nothing>>
        get() = _messageEvent

    private val _soundEvent =
        SingleLiveEvent<UnloadingScanSoundEvent<Nothing>>()
    val soundEvent: LiveData<UnloadingScanSoundEvent<Nothing>>
        get() = _soundEvent

    private val _unloadedState =
        MutableLiveData<UnloadingScanBoxState<Nothing>>()
    val unloadedState: LiveData<UnloadingScanBoxState<Nothing>>
        get() = _unloadedState

    private val _returnState =
        MutableLiveData<UnloadingReturnState<Nothing>>()
    val returnState: LiveData<UnloadingReturnState<Nothing>>
        get() = _returnState

    private val _navigationEvent =
        SingleLiveEvent<UnloadingScanNavAction>()
    val navigationEvent: LiveData<UnloadingScanNavAction>
        get() = _navigationEvent


    val bottomProgressEvent = MutableLiveData<Boolean>()

    init {
        observeScanProcess()
        observeUnloadedBoxes()
        observeReturnBoxes()
    }

    private fun observeScanProcess() {
        addSubscription(interactor.observeScanProcess(parameters.dstOfficeId).subscribe {
            when (it) {
                is UnloadingData.BoxAlreadyUnloaded -> {
                    _messageEvent.value =
                        UnloadingScanMessageEvent.BoxHasBeenAdded("Коробка ${it.barcode} уже выгружена")
                    _soundEvent.value = UnloadingScanSoundEvent.BoxSkipAdded
                }

                is UnloadingData.BoxAlreadyReturn -> {
                    _messageEvent.value =
                        UnloadingScanMessageEvent.BoxHasBeenAdded("Коробка ${it.barcode}\nуже добавлена к возврату")
                    _soundEvent.value = UnloadingScanSoundEvent.BoxSkipAdded
                }

                is UnloadingData.BoxUnloadAdded -> {
                    _messageEvent.value =
                        UnloadingScanMessageEvent.BoxAdded("Коробка ${it.barcode}\nготова к выгрузке")
                    _soundEvent.value = UnloadingScanSoundEvent.BoxAdded
                }

                is UnloadingData.BoxReturnAdded -> {
                    _messageEvent.value =
                        UnloadingScanMessageEvent.BoxAdded("Коробка ${it.barcode}\nготова к возврату")
                    _soundEvent.value = UnloadingScanSoundEvent.BoxAdded
                }

                is UnloadingData.BoxDoesNotBelongPoint -> {
                    _navigationEvent.value =
                        UnloadingScanNavAction.NavigateToUnloadingBoxNotBelongPoint(
                            resourceProvider.getBoxNotBelongPointToolbarTitle(),
                            resourceProvider.getBoxNotBelongPointTitle(),
                            it.barcode,
                            it.address)
                    _soundEvent.value = UnloadingScanSoundEvent.BoxSkipAdded
                }

                UnloadingData.Empty -> {
                }
            }

        })
    }

    private fun observeUnloadedBoxes() {
        addSubscription(interactor.observeUnloadedAndAttachedBoxes(parameters.dstOfficeId).subscribe({
            val uploadedList = it.first
            val listAttached = it.second
            val accepted = "" + uploadedList.size + "/" + (listAttached.size + uploadedList.size)

            if (uploadedList.isEmpty() && listAttached.isEmpty()) {
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
            _toolbarBackState.value = BackButtonState
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

    fun update() {
        _toolbarBackState.value = BackButtonState
        _toolbarLabelState.value = Label("Address")
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
                    addSubscription(interactor.completeUnloading(parameters.dstOfficeId).subscribe {
                        // TODO: 31.05.2021 уточнить навигацию
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

    fun onStartScanner() {
        interactor.scannerAction(ScannerAction.Start)
    }

    object BackButtonState

    data class Label(val label: String)

}