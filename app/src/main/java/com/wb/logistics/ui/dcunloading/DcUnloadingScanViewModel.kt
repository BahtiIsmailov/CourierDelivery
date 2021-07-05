package com.wb.logistics.ui.dcunloading

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.wb.logistics.network.exceptions.BadRequestException
import com.wb.logistics.network.exceptions.NoInternetException
import com.wb.logistics.ui.NetworkViewModel
import com.wb.logistics.ui.SingleLiveEvent
import com.wb.logistics.ui.dcunloading.domain.DcUnloadingData
import com.wb.logistics.ui.dcunloading.domain.DcUnloadingInteractor
import com.wb.logistics.ui.scanner.domain.ScannerAction
import com.wb.logistics.utils.LogUtils
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit

class DcUnloadingScanViewModel(
    compositeDisposable: CompositeDisposable,
    private val resourceProvider: DcUnloadingScanResourceProvider,
    private val interactor: DcUnloadingInteractor,
) : NetworkViewModel(compositeDisposable) {

    private val _toolbarBackState = MutableLiveData<BackButtonState>()
    val toolbarBackState: LiveData<BackButtonState>
        get() = _toolbarBackState

    private val _toolbarLabelState = MutableLiveData<Label>()
    val toolbarLabelState: LiveData<Label>
        get() = _toolbarLabelState

    private val _messageEvent = SingleLiveEvent<DcUnloadingScanMessageEvent>()
    val toastEvent: LiveData<DcUnloadingScanMessageEvent>
        get() = _messageEvent

    private val _soundEvent = SingleLiveEvent<DcUnloadingScanSoundEvent>()
    val soundEvent: LiveData<DcUnloadingScanSoundEvent>
        get() = _soundEvent

    private val _unloadedState = MutableLiveData<DcUnloadingScanBoxState>()
    val unloadedState: LiveData<DcUnloadingScanBoxState>
        get() = _unloadedState

    private val _unloadedCounterState = MutableLiveData<DcUnloadingScanCounterBoxState>()
    val unloadedCounterState: LiveData<DcUnloadingScanCounterBoxState>
        get() = _unloadedCounterState

    private val _navigationEvent = SingleLiveEvent<DcUnloadingScanNavAction>()
    val navigationEvent: LiveData<DcUnloadingScanNavAction>
        get() = _navigationEvent

    private val _navigateToMessageInfo = SingleLiveEvent<NavigateToMessageInfo>()
    val navigateToMessageInfo: LiveData<NavigateToMessageInfo>
        get() = _navigateToMessageInfo


    val bottomProgressEvent = MutableLiveData<Boolean>()

    init {
        observeScanProcess()
        observeDcUnloadedBoxes()
    }

    private fun observeScanProcess() {
        addSubscription(interactor.observeScanProcess()
            .doOnError { observeScanProcessError(it) }
            .retryWhen { errorObservable -> errorObservable.delay(1, TimeUnit.SECONDS) }
            .subscribe(observeScanProcessComplete()) { observeScanProcessError(it) }
        )
    }

    private fun observeScanProcessComplete(): (t: DcUnloadingData) -> Unit =
        {
            when (it) {
                is DcUnloadingData.BoxAlreadyUnloaded -> {
                    _messageEvent.value =
                        DcUnloadingScanMessageEvent.BoxAlreadyUnloaded(
                            resourceProvider.getBoxAlreadyUnloaded(it.barcode))
                    _soundEvent.value = DcUnloadingScanSoundEvent.BoxSkipAdded
                }

                is DcUnloadingData.BoxUnloaded -> {
                    _messageEvent.value =
                        DcUnloadingScanMessageEvent.BoxAdded(resourceProvider.getBoxUnloaded(it.barcode))
                    _soundEvent.value = DcUnloadingScanSoundEvent.BoxAdded
                }

                is DcUnloadingData.BoxDoesNotBelongDc -> {
                    _navigationEvent.value =
                        DcUnloadingScanNavAction.NavigateToUnloadingBoxNotBelongDc(
                            resourceProvider.getBoxNotFoundTitle())
                    _soundEvent.value = DcUnloadingScanSoundEvent.BoxSkipAdded
                    _unloadedState.value =
                        DcUnloadingScanBoxState.DcUnloadedBoxesNotBelong("неизвестный ШК")
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

    private fun observeDcUnloadedBoxes() {
        addSubscription(interactor.observeDcUnloadedBoxes().subscribe({
            val counter = it.first
            val barcode = it.second
            val accepted = with(counter) {
                resourceProvider.getBoxUnloadedCount(dcUnloadingCount,
                    dcUnloadingCount + dcReturnCount)
            }
            _unloadedCounterState.value = if (counter.dcUnloadingCount == 0)
                DcUnloadingScanCounterBoxState.DcUnloadedBoxesEmpty(accepted)
            else DcUnloadingScanCounterBoxState.DcUnloadedBoxesComplete(accepted, barcode)
            // TODO: 28.04.2021 выполнить автоматический переход после выгрузки всех коробок
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
            _navigationEvent.value = if (it.first.dcReturnCount == 0)
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

    data class NavigateToMessageInfo(val title: String, val message: String, val button: String)

}