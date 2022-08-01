package ru.wb.go.ui.courierloading

import CheckInternet
import android.content.Context
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
import ru.wb.go.app.AppPreffsKeys.CLOSE_FRAGMENT_WHEN_ENDED_TIME
import ru.wb.go.app.AppPreffsKeys.SAVE_LOCAL_TIME_WHEN_USER_DELETE_APP
import ru.wb.go.db.entity.courierlocal.LocalBoxEntity
import ru.wb.go.network.exceptions.NoInternetException
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
import ru.wb.go.utils.prefs.SharedWorker
import ru.wb.go.utils.time.DateTimeFormatter
import java.time.Duration
import java.time.LocalTime

class CourierLoadingScanViewModel(
    private val resourceProvider: CourierLoadingResourceProvider,
    private val interactor: CourierLoadingInteractor,
    private val courierOrderTimerInteractor: CourierOrderTimerInteractor,
    private val errorDialogManager: ErrorDialogManager,
    private val playManager: PlayManager,
    private val sharedWorker: SharedWorker,
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

    private val _startTimeForThreeHour = MutableLiveData<LocalTime>()
    private val _endTimeForThreeHour = MutableLiveData<LocalTime>()
    private val _endTimeOfCourierOrderAfterThreeHour = MutableLiveData<Boolean>()
    val endTimeOfCourierOrderAfterThreeHour: LiveData<Boolean> = _endTimeOfCourierOrderAfterThreeHour


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


    private fun timerForFoneModeCountTimeBetweenStartAndEndOrder(){
        viewModelScope.launch(Dispatchers.IO) {
            if (isActive) {
                delay(1000 * 60 * 2)//delay(1000 * 60 * 60 * 3)
                _endTimeOfCourierOrderAfterThreeHour.postValue(false)
            }
        }
    }


    private fun holdSplashScanner() {
        interactor.scannerAction(ScannerState.StopScanWithHoldSplash)
    }

    private fun sendRequestEveryFiveMinutes() {
        viewModelScope.launch(Dispatchers.IO) {
             while (isActive){
                 delay(1000 * 60 * 5)
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

    fun clearScannerState(){
        holdSplashScanner()
        interactor.clearScannerState()
    }

    fun checkInternetAndThenShow(context: Context){
        if (CheckInternet.checkConnection(context)){
            onConfirmLoadingClick()
            saveInfoIfCloseApp("")
        }else{
            errorDialogManager.showErrorDialog(NoInternetException(""), _navigateToDialogInfo)
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

    private fun scanProcessError(throwable: Throwable) {
        errorDialogManager.showErrorDialog(throwable, _navigateToDialogInfo)
    }

    private fun observeScanProcess() {
        interactor.observeScanProcess()
            .onEach {
                observeScanProcessComplete(it)
            }
            .retryWhen { it, _ ->
                scanProcessError(it)
                delay(1500)
                true
            }
            .launchIn(viewModelScope)
    }

    fun timeBetweenStartAndEndTask(){
        try {
            if (sharedWorker.isAllExists(SAVE_LOCAL_TIME_WHEN_USER_DELETE_APP)) {
                _startTimeForThreeHour.value = sharedWorker.load(
                    SAVE_LOCAL_TIME_WHEN_USER_DELETE_APP, LocalTime::class.java
                )
                _endTimeForThreeHour.value = LocalTime.now() // получаю текущее время
                val duration = Duration.between(
                    _startTimeForThreeHour.value,
                    _endTimeForThreeHour.value
                )
                Log.e("duration_time", "${duration.seconds}")
                if ((duration.seconds / 60L) >= 2L) { //(duration.seconds / 3600L) >= 3L)
                    _endTimeOfCourierOrderAfterThreeHour.value = false
                }
            }
        }catch (e:Exception){
            //_startTimeForThreeHour.value = LocalTime.now()
        }

    }

    fun saveInfoIfCloseApp(flag:String){
        sharedWorker.saveMediate(CLOSE_FRAGMENT_WHEN_ENDED_TIME,flag)
    }

    fun showBottomSheetAfterClose(){
        val response = sharedWorker.load(CLOSE_FRAGMENT_WHEN_ENDED_TIME,"")
        if (response != "") {
            _endTimeOfCourierOrderAfterThreeHour.value = response == "0"
        }
    }

    private suspend fun observeScanProcessComplete(scanResult: CourierLoadingProcessData) {
        timeBetweenStartAndEndTask()
        val scanBoxData = scanResult.scanBoxData
        val countBoxes = resourceProvider.getAccepted(scanResult.count)
        when (scanBoxData) {
            is CourierLoadingScanBoxData.FirstBoxAdded -> {
                timerForFoneModeCountTimeBetweenStartAndEndOrder()
                sharedWorker.save(SAVE_LOCAL_TIME_WHEN_USER_DELETE_APP,LocalTime.now())

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
                holdSplashScanner()
            }
            is CourierLoadingScanBoxData.SecondaryBoxAdded -> {
                _fragmentStateUI.value = CourierLoadingScanBoxState.LoadInCar
                _boxDataStateUI.value =
                    with(scanBoxData) { BoxInfoDataState(qrCode, address, countBoxes) }
                interactor.scanRepoHoldStart()
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
                interactor.scanRepoHoldStart()
            }
            CourierLoadingScanBoxData.NotRecognizedQr -> {
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
                interactor.scanRepoHoldStart()
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

    fun stopScanner() {
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

