package com.wb.logistics.ui.dcunloading

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.wb.logistics.network.exceptions.BadRequestException
import com.wb.logistics.network.exceptions.NoInternetException
import com.wb.logistics.network.monitor.NetworkState
import com.wb.logistics.ui.NetworkViewModel
import com.wb.logistics.ui.SingleLiveEvent
import com.wb.logistics.ui.dcloading.domain.ScanProgressData
import com.wb.logistics.ui.dcunloading.domain.DcUnloadingAction
import com.wb.logistics.ui.dcunloading.domain.DcUnloadingData
import com.wb.logistics.ui.dcunloading.domain.DcUnloadingInteractor
import com.wb.logistics.ui.scanner.domain.ScannerAction
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit

class DcUnloadingScanViewModel(
    compositeDisposable: CompositeDisposable,
    private val resourceProvider: DcUnloadingScanResourceProvider,
    private val interactor: DcUnloadingInteractor,
) : NetworkViewModel(compositeDisposable) {

    private val _toolbarNetworkState = MutableLiveData<NetworkState>()
    val toolbarNetworkState: LiveData<NetworkState>
        get() = _toolbarNetworkState

    private val _messageEvent = SingleLiveEvent<DcUnloadingScanMessageEvent>()
    val toastEvent: LiveData<DcUnloadingScanMessageEvent>
        get() = _messageEvent

    private val _soundEvent = SingleLiveEvent<DcUnloadingScanSoundEvent>()
    val soundEvent: LiveData<DcUnloadingScanSoundEvent>
        get() = _soundEvent

    private val _progressEvent =
        SingleLiveEvent<DcUnloadingScanProgress>()
    val progressEvent: LiveData<DcUnloadingScanProgress>
        get() = _progressEvent

    private val _unloadedState = MutableLiveData<DcUnloadedState>()
    val unloadedState: LiveData<DcUnloadedState>
        get() = _unloadedState

    private val _infoState = MutableLiveData<DcUnloadingInfoState>()
    val infoState: LiveData<DcUnloadingInfoState>
        get() = _infoState

    private val _navigationEvent = SingleLiveEvent<DcUnloadingScanNavAction>()
    val navigationEvent: LiveData<DcUnloadingScanNavAction>
        get() = _navigationEvent

    private val _navigateToMessageInfo = SingleLiveEvent<NavigateToMessageInfo>()
    val navigateToMessageInfo: LiveData<NavigateToMessageInfo>
        get() = _navigateToMessageInfo

    val bottomProgressEvent = MutableLiveData<Boolean>()

    init {
        observeNetworkState()
        observeScanProcess()
        observeScanProgress()
    }

    private fun observeScanProgress() {
        addSubscription(interactor.scanLoaderProgress()
            .subscribe {
                _progressEvent.value = when (it) {
                    ScanProgressData.Complete -> {
                        interactor.scannerAction(ScannerAction.LoaderComplete)
                        DcUnloadingScanProgress.LoaderComplete
                    }
                    ScanProgressData.Progress -> {
                        interactor.scannerAction(ScannerAction.LoaderProgress)
                        DcUnloadingScanProgress.LoaderProgress
                    }
                }
            })
    }

    private fun observeScanProcess() {
        addSubscription(interactor.observeUnloadingProcess()
            .doOnError { observeScanProcessError(it) }
            .retryWhen { errorObservable -> errorObservable.delay(1, TimeUnit.SECONDS) }
            .subscribe(observeScanProcessComplete()) { observeScanProcessError(it) }
        )
    }

    private fun observeScanProcessComplete(): (it: DcUnloadingData) -> Unit =
        {
            val accepted =
                resourceProvider.getAccepted(it.unloadedCounter.unloadedCount,
                    it.unloadedCounter.unloadedCount + it.unloadedCounter.leftUnload)
            when (it.dcUnloadingAction) {
                is DcUnloadingAction.BoxAlreadyUnloaded -> {
                    _messageEvent.value =
                        DcUnloadingScanMessageEvent.BoxAlreadyUnloaded(
                            resourceProvider.getBoxAlreadyUnloaded(it.dcUnloadingAction.barcode))
                    _soundEvent.value = DcUnloadingScanSoundEvent.BoxSkipAdded

                    _unloadedState.value = DcUnloadedState.Complete(accepted)
                    _infoState.value = DcUnloadingInfoState.Complete(it.dcUnloadingAction.barcode)
                }
                is DcUnloadingAction.BoxUnloaded -> {
                    _messageEvent.value =
                        DcUnloadingScanMessageEvent.BoxAdded(resourceProvider.getBoxUnloaded(it.dcUnloadingAction.barcode))
                    _soundEvent.value = DcUnloadingScanSoundEvent.BoxAdded
                    _unloadedState.value = DcUnloadedState.Active(accepted)
                    _infoState.value = DcUnloadingInfoState.Complete(it.dcUnloadingAction.barcode)
                }
                is DcUnloadingAction.BoxDoesNotBelongDc -> {
                    _navigationEvent.value =
                        DcUnloadingScanNavAction.NavigateToUnloadingBoxNotBelongDc(
                            resourceProvider.getBoxNotFoundTitle())
                    _soundEvent.value = DcUnloadingScanSoundEvent.BoxSkipAdded
                    _unloadedState.value = DcUnloadedState.Error(accepted)
                    _infoState.value =
                        DcUnloadingInfoState.Error(resourceProvider.getBoxNotBelong())
                }
                DcUnloadingAction.BoxDoesNotBelongFlight -> {
                    _navigationEvent.value =
                        DcUnloadingScanNavAction.NavigateToUnloadingBoxNotBelongDc(
                            resourceProvider.getBoxNotFoundTitle())
                    _soundEvent.value = DcUnloadingScanSoundEvent.BoxSkipAdded
                    _unloadedState.value = DcUnloadedState.Error(accepted)
                    _infoState.value =
                        DcUnloadingInfoState.Error(resourceProvider.getBoxNotBelong())
                }
                is DcUnloadingAction.BoxDoesNotBelongInfoEmpty -> {
                    _navigationEvent.value =
                        DcUnloadingScanNavAction.NavigateToUnloadingBoxNotBelongDc(
                            resourceProvider.getBoxNotFoundTitle())
                    _soundEvent.value = DcUnloadingScanSoundEvent.BoxSkipAdded
                    _unloadedState.value = DcUnloadedState.Error(accepted)
                    _infoState.value =
                        DcUnloadingInfoState.Error(resourceProvider.getBoxNotBelong())
                }
                DcUnloadingAction.Init -> {
                    if (it.unloadedCounter.unloadedCount > 0) {
                        _unloadedState.value = DcUnloadedState.Active(accepted)
                        _infoState.value =
                            DcUnloadingInfoState.Complete(it.unloadedCounter.barcode ?: "")
                    } else {
                        _unloadedState.value = DcUnloadedState.Complete(accepted)
                        _infoState.value = DcUnloadingInfoState.Empty
                    }
                }
            }
        }

    private fun observeScanProcessError(throwable: Throwable) {
        val message = when (throwable) {
            is NoInternetException -> throwable.message
            is BadRequestException -> throwable.error.message
            else -> resourceProvider.getScanDialogMessage()
        }
        interactor.scannerAction(ScannerAction.Stop)
        _navigateToMessageInfo.value = NavigateToMessageInfo(
            resourceProvider.getScanDialogTitle(), message, resourceProvider.getScanDialogButton())
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
        _progressEvent.value = DcUnloadingScanProgress.LoaderProgress
        addSubscription(interactor.isBoxesUnloaded()
            .flatMap {
                if (it) {
                    interactor.switchScreenToClosed()
                        .andThen(Single.just(DcUnloadingScanNavAction.NavigateToDcCongratulation))
                } else {
                    Single.just(DcUnloadingScanNavAction.NavigateToDcForcedTermination)
                }
            }.subscribe({
                _navigationEvent.value = it
                _progressEvent.value = DcUnloadingScanProgress.LoaderComplete
            }, { _progressEvent.value = DcUnloadingScanProgress.LoaderComplete })
        )
    }

    private fun observeNetworkState() {
        addSubscription(interactor.observeNetworkConnected()
            .subscribe({ _toolbarNetworkState.value = it }, {}))
    }

    fun onStopScanner() {
        interactor.scannerAction(ScannerAction.Stop)
    }

    fun onStartScanner() {
        interactor.scannerAction(ScannerAction.Start)
    }

    data class NavigateToMessageInfo(val title: String, val message: String, val button: String)

}