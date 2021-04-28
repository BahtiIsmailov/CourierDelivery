package com.wb.logistics.ui.unloading

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.wb.logistics.ui.NetworkViewModel
import com.wb.logistics.ui.SingleLiveEvent
import com.wb.logistics.ui.nav.domain.ScreenManager
import com.wb.logistics.ui.nav.domain.ScreenState
import com.wb.logistics.ui.scanner.domain.ScannerAction
import com.wb.logistics.ui.unloading.domain.UnloadingData
import com.wb.logistics.ui.unloading.domain.UnloadingInteractor
import com.wb.logistics.utils.LogUtils
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
        screenManager.saveScreenState(ScreenState.UNLOADING)
//        _toolbarState.value = UnloadingScanToolbarEvent.Label(parameters.shortAddress)
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
        addSubscription(interactor.observeUnloadedBoxes(parameters.dstOfficeId).subscribe({
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
        _toolbarLabelState.value = Label(parameters.shortAddress)
    }

    fun onBoxHandleInput(barcode: String) {
        interactor.barcodeManualInput(barcode.replace("-", ""))
    }

    fun onUnloadingListClicked() {
        _navigationEvent.value =
            UnloadingScanNavAction.NavigateToUploadedBoxes(parameters.dstOfficeId)
    }

    fun onReturnListClicked() {
        _navigationEvent.value = UnloadingScanNavAction.NavigateToReturnBoxes
    }

    fun onaHandleClicked() {
        _navigationEvent.value =
            UnloadingScanNavAction.NavigateToHandleInput(parameters.dstOfficeId)
    }

    fun onCompleteClicked() {
        // TODO: 28.04.2021 реализовать переход на следующий экран
//        toFlightDeliveries()

//        _navigationEvent.value =
//            UnloadingScanNavAction.NavigateToUnloadingBoxNotBelong("Не принадлежит ПВЗ",
//                "Коробка с другого ПВЗ,\nверните её в машину",
//                "TRBX-994827463",
//                "ПВЗ Москва, длинный адрес, который разошелся на 2 строки")

    }

    private fun toFlightDeliveries() {
        bottomProgressEvent.value = true
        addSubscription(interactor.sendAwaitBoxes().subscribe({
            if (it > 0) {
                bottomProgressEvent.value = false
            } else {
                screenManager.saveScreenState(ScreenState.FLIGHT_PICK_UP_POINT)
                _navigationEvent.value = UnloadingScanNavAction.NavigateToFlightDeliveries
                bottomProgressEvent.value = false
            }

        }, {
            bottomProgressEvent.value = false
        }))
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