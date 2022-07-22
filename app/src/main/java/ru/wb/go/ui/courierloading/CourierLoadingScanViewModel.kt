package ru.wb.go.ui.courierloading

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import ru.wb.go.db.entity.courierlocal.LocalBoxEntity
import ru.wb.go.ui.ServicesViewModel
import ru.wb.go.ui.SingleLiveEvent
import ru.wb.go.ui.auth.signup.TimerState
import ru.wb.go.ui.auth.signup.TimerStateHandler
import ru.wb.go.ui.courierloading.domain.CourierCompleteData
import ru.wb.go.ui.courierloading.domain.CourierLoadingInteractor
import ru.wb.go.ui.courierloading.domain.CourierLoadingProcessData
import ru.wb.go.ui.courierloading.domain.CourierLoadingScanBoxData
import ru.wb.go.ui.courierordertimer.domain.CourierOrderTimerInteractor
import ru.wb.go.ui.dialogs.DialogInfoStyle
import ru.wb.go.ui.scanner.domain.ScannerState
import ru.wb.go.utils.WaitLoader
import ru.wb.go.utils.managers.ErrorDialogData
import ru.wb.go.utils.managers.ErrorDialogManager
import ru.wb.go.utils.managers.PlayManager
import ru.wb.go.utils.time.DateTimeFormatter

class CourierLoadingScanViewModel(
    private val resourceProvider: CourierLoadingResourceProvider,
    private val interactor: CourierLoadingInteractor,
    private val courierOrderTimerInteractor: CourierOrderTimerInteractor,
    private val errorDialogManager: ErrorDialogManager,
    private val playManager: PlayManager,
) : TimerStateHandler,
    ServicesViewModel(interactor, resourceProvider) {

    private val _orderTimer = MutableLiveData<CourierLoadingScanTimerState>()
    val orderTimer: LiveData<CourierLoadingScanTimerState>
        get() = _orderTimer

    private val _navigationEvent =
        SingleLiveEvent<CourierLoadingScanNavAction>()
    val navigationEvent: LiveData<CourierLoadingScanNavAction>
        get() = _navigationEvent

    private val _navigateToDialogInfo = SingleLiveEvent<ErrorDialogData>()
    val navigateToDialogInfo: LiveData<ErrorDialogData>
        get() = _navigateToDialogInfo

    private val _beepEvent =
        SingleLiveEvent<CourierLoadingScanBeepState>()
    val beepEvent: LiveData<CourierLoadingScanBeepState>
        get() = _beepEvent

    private val _waitLoader =
        SingleLiveEvent<WaitLoader>()
    val waitLoader: LiveData<WaitLoader>
        get() = _waitLoader

    private val _fragmentStateUI =
        MutableLiveData<CourierLoadingScanBoxState>()
    val fragmentStateUI: LiveData<CourierLoadingScanBoxState>
        get() = _fragmentStateUI

    private val _boxDataStateUI =
        MutableLiveData<BoxInfoDataState>()
    val boxDataStateUI: LiveData<BoxInfoDataState>
        get() = _boxDataStateUI

    private val _completeButtonState = SingleLiveEvent<Boolean>()
    val completeButtonState: LiveData<Boolean>
        get() = _completeButtonState

    private val _dublicateBoxId = MutableLiveData<Boolean>()
    val dublicateBoxId: LiveData<Boolean> = _dublicateBoxId


    private val _timeOut = SingleLiveEvent<Boolean>()
        .apply { value = false }
    val timeOut: LiveData<Boolean>
        get() = _timeOut




    init {
        observeInitScanProcess()
        observeScanProcess()
        getGate()
        holdSplashScanner()
        sendRequestEveryFiveMinutes()
    }

    private fun holdSplashScanner() {
        interactor.scannerAction(ScannerState.StopScanWithHoldSplash)
    }

    private fun sendRequestEveryFiveMinutes() {
        viewModelScope.launch(Dispatchers.IO) {
             while (isActive){
                 delay(1000 * 10)
                 try {
                     interactor.confirmLoadingBoxesEveryFiveMinutes()
                 } catch (e: Exception) {

                 }
             }
        }
    }


    fun onConfirmLoadingClick() {
        setLoader(WaitLoader.Wait)
        viewModelScope.launch {
            try {
                val response = interactor.confirmLoadingBoxes()
                setLoader(WaitLoader.Complete)
                confirmLoadingBoxesComplete(response)
            } catch (e: Exception) {
                logException(e,"onConfirmLoadingClick")
                setLoader(WaitLoader.Complete)
                errorDialogManager.showErrorDialog(e, _navigateToDialogInfo)
            }
        }
    }
    private fun getGate() {
        viewModelScope.launch {
            try {
                val response = interactor.getGate()
                _orderTimer.value =
                    CourierLoadingScanTimerState.Info(response?.ifEmpty { "-" }?:"")

            } catch (e: Exception) {
                logException(e,"getGate")
                _orderTimer.value = CourierLoadingScanTimerState.Info("-")
            }
        }
    }

    fun observeTimer() {
        courierOrderTimerInteractor.timer
            .onEach {
                observeTimerComplete(it)
            }
            .catch {
                logException(it,"observeTimer")
            }
            .launchIn(viewModelScope)

    }

    private fun observeTimerComplete(timerState: TimerState) {
        timerState.handle(this)
    }

    private fun observeInitScanProcess() {
        viewModelScope.launch {
            try {
                val response = interactor.scannedBoxes()
                initScanProcessComplete(response)
            } catch (e: Exception) {
                logException(e,"observeInitScanProcess")
            }
        }
    }


    private fun initScanProcessComplete(boxes: List<LocalBoxEntity>) {
        if (boxes.isEmpty()) {
            observeTimer()
            _fragmentStateUI.value = CourierLoadingScanBoxState.InitScanner
        } else {
            val lastBox = boxes.last()
            _boxDataStateUI.value =
                BoxInfoDataState(
                    lastBox.boxId,
                    lastBox.address,
                    resourceProvider.getAccepted(boxes.size),
                )

            _fragmentStateUI.value = CourierLoadingScanBoxState.LoadInCar
            _completeButtonState.value = boxes.isNotEmpty()
        }
    }

    private fun observeScanProcess() {
        interactor.observeScanProcess()
            .onEach {
                observeScanProcessComplete(it)
                interactor.scanRepoHoldStart()
            }
            .retryWhen { it, _ ->
                scanProcessError(it)
                delay(1500)
                true
            }
            .launchIn(viewModelScope)
    }


    private fun scanProcessError(throwable: Throwable) {
        errorDialogManager.showErrorDialog(throwable, _navigateToDialogInfo)
    }

    private fun observeScanProcessComplete(scanResult: CourierLoadingProcessData) {
        val scanBoxData = scanResult.scanBoxData
        val countBoxes = resourceProvider.getAccepted(scanResult.count)
        when (scanBoxData) {
            is CourierLoadingScanBoxData.FirstBoxAdded -> {
                _fragmentStateUI.value = CourierLoadingScanBoxState.LoadInCar
                _boxDataStateUI.value =
                    with(scanBoxData) {
                        BoxInfoDataState(qrCode, address, countBoxes)
                    }
                setLogBoxesQrCodeAddressAndCount(scanBoxData.qrCode,scanBoxData.address,countBoxes)
                setValueToStartLog(true)
                 _beepEvent.value = CourierLoadingScanBeepState.BoxFirstAdded
                _orderTimer.value = CourierLoadingScanTimerState.Stopped
                _completeButtonState.value = true
            }
            is CourierLoadingScanBoxData.SecondaryBoxAdded -> {
                _fragmentStateUI.value = CourierLoadingScanBoxState.LoadInCar
                _boxDataStateUI.value =
                    with(scanBoxData) { BoxInfoDataState(qrCode, address, countBoxes) }
            }
            is CourierLoadingScanBoxData.ForbiddenTakeBox -> {
                if (scanResult.count == 0) {
                    _fragmentStateUI.value = CourierLoadingScanBoxState.ForbiddenTakeWithTimer
                } else {
                    _fragmentStateUI.value = CourierLoadingScanBoxState.ForbiddenTakeBox
                    _boxDataStateUI.value = with(scanBoxData) {
                        BoxInfoDataState(
                            qrCode,
                            resourceProvider.getEmptyAddress(),
                            countBoxes
                        )
                    }
                }
                _beepEvent.value = CourierLoadingScanBeepState.UnknownBox
            }
            is CourierLoadingScanBoxData.NotRecognizedQr -> {
                if (scanResult.count == 0) {
                    _fragmentStateUI.value = CourierLoadingScanBoxState.NotRecognizedQrWithTimer
                } else {
                    _fragmentStateUI.value = CourierLoadingScanBoxState.NotRecognizedQr
                    _boxDataStateUI.value = BoxInfoDataState(
                        resourceProvider.getUnknown(),
                        resourceProvider.getEmptyAddress(),
                        countBoxes
                    )
                }
                _beepEvent.value = CourierLoadingScanBeepState.UnknownQR
            }
        }
    }

    fun onErrorDialogConfirmClick() {
        onStartScanner()
        _completeButtonState.value = true
    }

    private fun setLoader(state: WaitLoader) {
        _waitLoader.value = state
    }



    private fun confirmLoadingBoxesComplete(courierCompleteData: CourierCompleteData) {
        onCleared()
        _navigationEvent.value =
            CourierLoadingScanNavAction.NavigateToStartDelivery(
                courierCompleteData.amount,
                courierCompleteData.countBox

            )
    }

    fun onCancelLoadingClick() {
        onStartScanner()
        _completeButtonState.value = true
    }

    fun onCompleteLoaderClicked() {
        _completeButtonState.value = false
        _navigationEvent.value = CourierLoadingScanNavAction.NavigateToConfirmDialog
        stopScanner()
    }

    fun onCounterBoxClicked() {
        stopScanner()
        viewModelScope.launch {
            try {
                val loadingBoxes = interactor.loadingBoxBoxesGroupByOffice()
                val items = mutableListOf<CourierLoadingDetailsItem>()
                loadingBoxes.localLoadingBoxEntity.forEach {
                    items.add(
                        CourierLoadingDetailsItem(
                            it.address,
                            resourceProvider.getAccepted(it.count)
                        )
                    )
                }
                _navigationEvent.value =
                    CourierLoadingScanNavAction.InitAndShowLoadingItems(
                        resourceProvider.getPvzCountTitle(loadingBoxes.pvzCount),
                        resourceProvider.getBoxCountTitle(loadingBoxes.boxCount),
                        items
                    )

            } catch (e: Exception) {
                logException(e,"onCounterBoxClicked")
            }
        }

    }
     fun onCloseDetailsClick() {
        onStartScanner()
        _navigationEvent.value = CourierLoadingScanNavAction.HideLoadingItems
    }

    fun onStartScanner() {
        interactor.scannerAction(ScannerState.StartScan)
    }

    private fun stopScanner() {
        interactor.scannerAction(ScannerState.StopScan)
    }

    override fun onTimerState(duration: Int, downTickSec: Int) {
        updateTimer(duration, downTickSec)
    }

    private fun updateTimer(duration: Int, downTickSec: Int) {
        _orderTimer.value =
            CourierLoadingScanTimerState.Timer(
                DateTimeFormatter.getAnalogTime(duration, downTickSec),
                DateTimeFormatter.getDigitTime(downTickSec)
            )
    }

    override fun onTimeIsOverState() {
        _timeOut.value = true
        stopScanner()
        _orderTimer.value =
            CourierLoadingScanTimerState.TimeIsOut(
                DialogInfoStyle.WARNING.ordinal,
                resourceProvider.getScanDialogTimeIsOutTitle(),
                resourceProvider.getScanDialogTimeIsOutMessage(),
                resourceProvider.getScanDialogTimeIsOutButton()
            )

    }

    fun returnToListOrderClick() {
        deleteTask()
    }

    private fun deleteTask() {
        setLoader(WaitLoader.Wait)
        viewModelScope.launch {
            try {
                interactor.deleteTask()
                setLoader(WaitLoader.Complete)
                toWarehouse()
                _timeOut.value = false
            } catch (e: Exception) {
                logException(e,"deleteTask")
                setLoader(WaitLoader.Complete)
                errorDialogManager.showErrorDialog(e, _navigateToDialogInfo)
            }
        }
    }

    private fun toWarehouse() {
        _navigationEvent.value = CourierLoadingScanNavAction.NavigateToWarehouse
    }

    fun play(resId: Int) {
        playManager.play(resId)
    }

    override fun getScreenTag(): String {
        return SCREEN_TAG
    }

    companion object {
        const val SCREEN_TAG = "CourierLoadingScan"
    }

}

