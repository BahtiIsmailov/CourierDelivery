package com.wb.logistics.ui.dcunloading

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.wb.logistics.ui.NetworkViewModel
import com.wb.logistics.ui.SingleLiveEvent
import com.wb.logistics.ui.dcunloading.domain.DcUnloadingData
import com.wb.logistics.ui.dcunloading.domain.DcUnloadingInteractor
import com.wb.logistics.ui.scanner.domain.ScannerAction
import com.wb.logistics.utils.LogUtils
import com.wb.logistics.utils.managers.ScreenManager
import io.reactivex.disposables.CompositeDisposable

class DcUnloadingScanViewModel(
    compositeDisposable: CompositeDisposable,
    private val resourceProvider: DcUnloadingScanResourceProvider,
    private val interactor: DcUnloadingInteractor,
    private val screenManager: ScreenManager,
) : NetworkViewModel(compositeDisposable) {

    private val _toolbarBackState = MutableLiveData<BackButtonState>()
    val toolbarBackState: LiveData<BackButtonState>
        get() = _toolbarBackState

    private val _toolbarLabelState = MutableLiveData<Label>()
    val toolbarLabelState: LiveData<Label>
        get() = _toolbarLabelState

    private val _messageEvent =
        SingleLiveEvent<DcUnloadingScanMessageEvent>()
    val toastEvent: LiveData<DcUnloadingScanMessageEvent>
        get() = _messageEvent

    private val _soundEvent =
        SingleLiveEvent<DcUnloadingScanSoundEvent>()
    val soundEvent: LiveData<DcUnloadingScanSoundEvent>
        get() = _soundEvent

    private val _unloadedState =
        MutableLiveData<DcUnloadingScanBoxState>()
    val unloadedState: LiveData<DcUnloadingScanBoxState>
        get() = _unloadedState

    private val _navigationEvent =
        SingleLiveEvent<DcUnloadingScanNavAction>()
    val navigationEvent: LiveData<DcUnloadingScanNavAction>
        get() = _navigationEvent


    val bottomProgressEvent = MutableLiveData<Boolean>()

    init {
        observeScanProcess()
        observeDcUnloadedBoxes()
    }

    private fun observeScanProcess() {
        addSubscription(interactor.observeScanProcess().subscribe {
            when (it) {
                is DcUnloadingData.BoxAlreadyUnloaded -> {
                    _messageEvent.value =
                        DcUnloadingScanMessageEvent.BoxAlreadyUnloaded(
                            resourceProvider.getBoxAlreadyUnloaded(it.barcode))
                    _soundEvent.value = DcUnloadingScanSoundEvent.BoxSkipAdded
                }

                is DcUnloadingData.BoxUnload -> {
                    _messageEvent.value =
                        DcUnloadingScanMessageEvent.BoxAdded(resourceProvider.getBoxUnloaded(it.barcode))
                    _soundEvent.value = DcUnloadingScanSoundEvent.BoxAdded
                }

                is DcUnloadingData.BoxDoesNotBelongDc -> {
                    _navigationEvent.value =
                        DcUnloadingScanNavAction.NavigateToUnloadingBoxNotBelongDc(
                            resourceProvider.getBoxNotFoundTitle())
                    _soundEvent.value = DcUnloadingScanSoundEvent.BoxSkipAdded
                }

            }

        })
    }

    private fun observeDcUnloadedBoxes() {
        addSubscription(interactor.observeDcUnloadedBoxes().subscribe({
            val accepted =
                "" + it.dcUnloadingCount + "/" + (it.dcUnloadingCount + it.attachedCount + it.returnCount)
            if (it.dcUnloadingCount == 0) {
                _unloadedState.value =
                    DcUnloadingScanBoxState.DcUnloadedBoxesEmpty(accepted)
                return@subscribe
            } else {
                _unloadedState.value =
                    DcUnloadingScanBoxState.DcUnloadedBoxesComplete(accepted, it.barcode)
                return@subscribe
            }
            // TODO: 28.04.2021 выполнить переход после выгрузки всех коробок
        }, {
            LogUtils { logDebugApp(it.toString()) }
        }))
    }

    fun update() {
        _toolbarBackState.value = BackButtonState
    }

    fun onBoxHandleInput(barcode: String) {
        interactor.barcodeManualInput(barcode.replace("-", ""))
    }

    fun onUnloadingListClicked() {
        _navigationEvent.value = DcUnloadingScanNavAction.NavigateToDcUploadedBoxes
    }

    fun onaHandleClicked() {
        _navigationEvent.value = DcUnloadingScanNavAction.NavigateToDcHandleInput
    }

    fun onCompleteClicked() {
        addSubscription(interactor.observeDcUnloadedBoxes().subscribe({
            _navigationEvent.value = if (it.attachedCount == 0 && it.returnCount == 0)
                DcUnloadingScanNavAction.NavigateToDcCongratulation
            else DcUnloadingScanNavAction.NavigateToDcForcedTermination
        }, {}))
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