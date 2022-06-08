package ru.wb.go.ui.courierloading

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.delay
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
import ru.wb.go.utils.analytics.YandexMetricManager
import ru.wb.go.utils.managers.ErrorDialogData
import ru.wb.go.utils.managers.ErrorDialogManager
import ru.wb.go.utils.managers.PlayManager
import ru.wb.go.utils.time.DateTimeFormatter

class CourierLoadingScanViewModel(
    compositeDisposable: CompositeDisposable,
    metric: YandexMetricManager,
    private val resourceProvider: CourierLoadingResourceProvider,
    private val interactor: CourierLoadingInteractor,
    private val courierOrderTimerInteractor: CourierOrderTimerInteractor,
    private val errorDialogManager: ErrorDialogManager,
    private val playManager: PlayManager,
) : TimerStateHandler,
    ServicesViewModel(compositeDisposable, metric, interactor, resourceProvider) {

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

    private val _timeOut = SingleLiveEvent<Boolean>()
        .apply { value = false }
    val timeOut: LiveData<Boolean>
        get() = _timeOut

    init {
        observeInitScanProcess()
        observeScanProcess()
        getGate()
        holdSplashScanner()
    }

    private fun holdSplashScanner() {
        interactor.scannerAction(ScannerState.StopScanWithHoldSplash)
    }

    private fun getGate() {
        viewModelScope.launch {
            try {
                val response = interactor.getGate()
                _orderTimer.postValue(
                    CourierLoadingScanTimerState.Info(response.ifEmpty { "-" }))
            }catch (e:Exception){
                _orderTimer.postValue(CourierLoadingScanTimerState.Info("-"))
            }
        }
    }
//    private fun getGate() {
//        addSubscription(
//            interactor.getGate()
//                .subscribe(
//                    {
//                        _orderTimer.postValue(
//                            CourierLoadingScanTimerState.Info(it.ifEmpty { "-" })
//                    },
//                    { _orderTimer.postValue( CourierLoadingScanTimerState.Info("-") })
//        )
//    }

    private fun observeTimer() {
        addSubscription(
            courierOrderTimerInteractor.timer
                .subscribe({ observeTimerComplete(it) }, { observeTimerError(it) })
        )
    }

    private fun observeTimerComplete(timerState: TimerState) {
        timerState.handle(this)
    }

    private fun observeTimerError(throwable: Throwable) {
        onTechErrorLog("observeTimerError", throwable)
    }

    private fun observeInitScanProcess() {
        viewModelScope.launch {
            try {
                val response = interactor.scannedBoxes()
                initScanProcessComplete(response)
            }catch (e:Exception){
                initScanProcessError(e)
            }
        }
    }

    private fun initScanProcessComplete(boxes: List<LocalBoxEntity>) {
        onTechEventLog("initScanProcessComplete", "countBox " + boxes.size)
        if (boxes.isEmpty()) {
            observeTimer()
            _fragmentStateUI.postValue(CourierLoadingScanBoxState.InitScanner)
        } else {
            val lastBox = boxes.last()
            _boxDataStateUI.postValue(BoxInfoDataState(
                lastBox.boxId,
                lastBox.address,
                resourceProvider.getAccepted(boxes.size),
            ))
            _fragmentStateUI.postValue(CourierLoadingScanBoxState.LoadInCar)
            _completeButtonState.postValue(boxes.isNotEmpty())
        }
    }

    private fun initScanProcessError(it: Throwable) {
        onTechErrorLog("initScanProcessError", it)
    }

    private fun observeScanProcess() {
        viewModelScope.launch {
            try {
                val response = interactor.observeScanProcess()
                observeScanProcessComplete(response)
            }catch (e:Exception){
                delay(1000)
                scanProcessError(e)
            }
        }
    }

    private fun scanProcessError(throwable: Throwable) {
        onTechErrorLog("observeScanProcessError", throwable)
        errorDialogManager.showErrorDialog(throwable, _navigateToDialogInfo)
    }

    private fun observeScanProcessComplete(scanResult: CourierLoadingProcessData) {
        onTechEventLog(
            "observeScanProcessComplete",
            scanResult.scanBoxData.toString() + " " + scanResult.count
        )
        val scanBoxData = scanResult.scanBoxData
        val countBoxes = resourceProvider.getAccepted(scanResult.count)

        when (scanBoxData) {
            is CourierLoadingScanBoxData.FirstBoxAdded -> {
                _fragmentStateUI.postValue( CourierLoadingScanBoxState.LoadInCar)
                _boxDataStateUI.postValue(
                    with(scanBoxData) { BoxInfoDataState(qrCode, address, countBoxes) })
                _beepEvent.postValue( CourierLoadingScanBeepState.BoxFirstAdded)
                _orderTimer.postValue( CourierLoadingScanTimerState.Stopped)
                _completeButtonState.postValue( true)
            }
            is CourierLoadingScanBoxData.SecondaryBoxAdded -> {
                _fragmentStateUI.postValue( CourierLoadingScanBoxState.LoadInCar)
                _boxDataStateUI.postValue(
                    with(scanBoxData) { BoxInfoDataState(qrCode, address, countBoxes) })
            }
            is CourierLoadingScanBoxData.ForbiddenTakeBox -> {
                if (scanResult.count == 0) {
                    _fragmentStateUI.postValue( CourierLoadingScanBoxState.ForbiddenTakeWithTimer)
                } else {
                    _fragmentStateUI.postValue( CourierLoadingScanBoxState.ForbiddenTakeBox)
                    _boxDataStateUI.postValue( with(scanBoxData) {
                        BoxInfoDataState(
                            qrCode,
                            resourceProvider.getEmptyAddress(),
                            countBoxes
                        )
                    })
                }
                _beepEvent.postValue( CourierLoadingScanBeepState.UnknownBox)
            }
            is CourierLoadingScanBoxData.NotRecognizedQr -> {
                if (scanResult.count == 0) {
                    _fragmentStateUI.postValue( CourierLoadingScanBoxState.NotRecognizedQrWithTimer)
                } else {
                    _fragmentStateUI.postValue( CourierLoadingScanBoxState.NotRecognizedQr)
                    _boxDataStateUI.postValue( BoxInfoDataState(
                        resourceProvider.getUnknown(),
                        resourceProvider.getEmptyAddress(),
                        countBoxes
                    ))
                }
                _beepEvent.postValue( CourierLoadingScanBeepState.UnknownQR)
            }
        }
    }

    fun onErrorDialogConfirmClick() {
        onStartScanner()
        _completeButtonState.postValue( true)
    }

    private fun setLoader(state: WaitLoader) {
        _waitLoader.postValue(state)
    }

    fun onConfirmLoadingClick() {
        setLoader(WaitLoader.Wait)
        viewModelScope.launch {
            try {
                val response = interactor.confirmLoadingBoxes()
                setLoader(WaitLoader.Complete)
                confirmLoadingBoxesComplete(response)
            }catch (e:Exception){
                setLoader(WaitLoader.Complete)
                onTechErrorLog("confirmLoadingBoxesError", e)
                errorDialogManager.showErrorDialog(e, _navigateToDialogInfo)
            }
        }
    }

    private fun confirmLoadingBoxesComplete(courierCompleteData: CourierCompleteData) {
        onCleared()
        _navigationEvent.postValue( CourierLoadingScanNavAction.NavigateToStartDelivery(
            courierCompleteData.amount,
            courierCompleteData.countBox
        ))
    }

    fun onCancelLoadingClick() {
        onStartScanner()
        _completeButtonState.postValue( true)
    }

    fun onCompleteLoaderClicked() {
        val stop = Single.just(stopScanner())
        _completeButtonState.postValue(false)
        _navigationEvent.postValue(CourierLoadingScanNavAction.NavigateToConfirmDialog)
        addSubscription(
            stop.subscribe()
        )
    }

    fun onCounterBoxClicked() {
        stopScanner()
        addSubscription(
            interactor.loadingBoxBoxesGroupByOffice()
                .map { loadingBoxes ->
                    val items = mutableListOf<CourierLoadingDetailsItem>()
                    loadingBoxes.localLoadingBoxEntity.forEach {
                        items.add(
                            CourierLoadingDetailsItem(
                                it.address,
                                resourceProvider.getAccepted(it.count)
                            )
                        )
                    }
                    CourierLoadingScanNavAction.InitAndShowLoadingItems(
                        resourceProvider.getPvzCountTitle(loadingBoxes.pvzCount),
                        resourceProvider.getBoxCountTitle(loadingBoxes.boxCount),
                        items
                    )
                }.subscribe(
                    {
                        _navigationEvent.postValue( it)
                    }, {

                    }
                )
        )
    }

    fun onCloseDetailsClick() {
        onStartScanner()
        _navigationEvent.postValue( CourierLoadingScanNavAction.HideLoadingItems)
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
        _orderTimer.postValue(
            CourierLoadingScanTimerState.Timer(
                DateTimeFormatter.getAnalogTime(duration, downTickSec),
                DateTimeFormatter.getDigitTime(downTickSec)
            ))
    }

    override fun onTimeIsOverState() {
        onTechEventLog("onTimeIsOverState")
        _timeOut.postValue(true)
        stopScanner()
        _orderTimer.postValue( CourierLoadingScanTimerState.TimeIsOut(
            DialogInfoStyle.WARNING.ordinal,
            resourceProvider.getScanDialogTimeIsOutTitle(),
            resourceProvider.getScanDialogTimeIsOutMessage(),
            resourceProvider.getScanDialogTimeIsOutButton()
        ))
    }

    fun returnToListOrderClick() {
        onTechEventLog("returnToListOrderClick")
        deleteTask()
    }

    private fun deleteTask() {
        setLoader(WaitLoader.Wait)
        viewModelScope.launch {
            try {
                interactor.deleteTask()
                setLoader(WaitLoader.Complete)
                onTechEventLog("toWarehouse")
                toWarehouse()
                _timeOut.postValue(false)
            }catch (e:Exception){
                setLoader(WaitLoader.Complete)
                errorDialogManager.showErrorDialog(e, _navigateToDialogInfo)
            }
        }
    }

    private fun toWarehouse() {
        _navigationEvent.postValue( CourierLoadingScanNavAction.NavigateToWarehouse)
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