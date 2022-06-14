package ru.wb.go.ui.courierunloading

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.launch
import ru.wb.go.db.entity.courierlocal.LocalBoxEntity
import ru.wb.go.db.entity.courierlocal.LocalOfficeEntity
import ru.wb.go.ui.ServicesViewModel
import ru.wb.go.ui.SingleLiveEvent
import ru.wb.go.ui.courierunloading.domain.CourierUnloadingInteractor
import ru.wb.go.ui.courierunloading.domain.CourierUnloadingProcessData
import ru.wb.go.ui.courierunloading.domain.CourierUnloadingProgressData
import ru.wb.go.ui.courierunloading.domain.CourierUnloadingScanBoxData
import ru.wb.go.ui.dialogs.DialogInfoStyle
import ru.wb.go.ui.dialogs.NavigateToDialogConfirmInfo
import ru.wb.go.ui.dialogs.NavigateToDialogInfo
import ru.wb.go.ui.scanner.domain.ScannerState
import ru.wb.go.utils.WaitLoader
import ru.wb.go.utils.analytics.YandexMetricManager
import ru.wb.go.utils.managers.ErrorDialogData
import ru.wb.go.utils.managers.ErrorDialogManager
import ru.wb.go.utils.managers.PlayManager

class CourierUnloadingScanViewModel(
    private val parameters: CourierUnloadingScanParameters,
    compositeDisposable: CompositeDisposable,
    metric: YandexMetricManager,
    private val resourceProvider: CourierUnloadingResourceProvider,
    private val interactor: CourierUnloadingInteractor,
    private val errorDialogManager: ErrorDialogManager,
    private val playManager: PlayManager,
) : ServicesViewModel(compositeDisposable, metric, interactor, resourceProvider) {

    private val _toolbarLabelState = MutableLiveData<Label>()
    val toolbarLabelState: LiveData<Label>
        get() = _toolbarLabelState

    private val _navigationEvent =
        SingleLiveEvent<CourierUnloadingScanNavAction>()
    val navigationEvent: LiveData<CourierUnloadingScanNavAction>
        get() = _navigationEvent

    private val _navigateToDialogInfo = SingleLiveEvent<ErrorDialogData>()
    val navigateToDialogInfo: LiveData<ErrorDialogData>
        get() = _navigateToDialogInfo

    private val _navigateToDialogScoreError = SingleLiveEvent<NavigateToDialogInfo>()
    val navigateToDialogScoreError: LiveData<NavigateToDialogInfo>
        get() = _navigateToDialogScoreError

    private val _navigateToDialogConfirmScoreInfo = SingleLiveEvent<NavigateToDialogConfirmInfo>()
    val navigateToDialogConfirmScoreInfo: LiveData<NavigateToDialogConfirmInfo>
        get() = _navigateToDialogConfirmScoreInfo

    private val _beepEvent = SingleLiveEvent<CourierUnloadingScanBeepState>()
    val beepEvent: LiveData<CourierUnloadingScanBeepState>
        get() = _beepEvent

    private val _waitLoader = SingleLiveEvent<WaitLoader>()
    val waitLoader: LiveData<WaitLoader> get() = _waitLoader

    private val _fragmentStateUI =
        MutableLiveData<UnloadingFragmentState>()
    val fragmentStateUI: LiveData<UnloadingFragmentState>
        get() = _fragmentStateUI

    private val _orderState = MutableLiveData<String>()
    val orderState: LiveData<String>
        get() = _orderState

    private val _completeButtonEnable = SingleLiveEvent<Boolean>()
    val completeButtonEnable: LiveData<Boolean>
        get() = _completeButtonEnable

    fun update() {
        initTitle()
        initToolbar()
        observeBoxInfoProcessInitState()
        observeScanProcess()
        observeScanProgress()
    }

    private fun initTitle() {
        viewModelScope.launch {
            _orderState.postValue(resourceProvider.getOrderId(interactor.getOrderId()))
        }
    }

    private fun holdSplashScanner() {
        viewModelScope.launch {
            interactor.scannerAction(ScannerState.StopScanWithHoldSplash)
        }
    }

    private fun observeBoxInfoProcessInitState() {
        viewModelScope.launch {
            try {
                _fragmentStateUI.postValue(mapInitScanProcess(interactor.getCurrentOffice(parameters.officeId)))

            }catch (e:Exception){
                onTechErrorLog("observeInitScanProcessError", e)
            }
        }

    }

    private fun mapInitScanProcess(office: LocalOfficeEntity): UnloadingFragmentState {
        val readyStatus = resourceProvider.getReadyStatus()
        val accepted = resourceProvider.getAccepted(
            office.deliveredBoxes,
            office.countBoxes
        )
        return UnloadingFragmentState.Empty(
            UnloadingFragmentData(
                readyStatus,
                resourceProvider.getEmptyQr(),
                resourceProvider.getEmptyAddress(),
                accepted
            )
        )
    }
    private fun initToolbar() {
        viewModelScope.launch {
            try {
                _toolbarLabelState.postValue( Label(interactor.getCurrentOffice(parameters.officeId).officeName))
            }catch (e:Exception){
                onTechErrorLog("initToolbar", e)
            }
        }

    }
     fun onCancelScoreUnloadingClick() {
        onTechEventLog("onCancelScoreUnloadingClick")
        _completeButtonEnable.postValue( true)
        setLoader(WaitLoader.Complete)
        onStartScanner()
    }

    private fun setLoader(state: WaitLoader) {
        _waitLoader.postValue(state)
    }

    fun onDestroy() {
        clearSubscription()
    }

    fun onConfirmScoreUnloadingClick() {
        onTechEventLog("onConfirmScoreUnloadingClick")
        confirmUnloading()
    }

    private fun confirmUnloading() {
        setLoader(WaitLoader.Wait)
        viewModelScope.launch {
            try {
                interactor.completeOfficeUnload()
                _navigationEvent.postValue(CourierUnloadingScanNavAction.NavigateToIntransit)
                setLoader(WaitLoader.Complete)
                clearSubscription()
            }catch (e:Exception){
                onTechErrorLog("confirmUnload", e)
            }
        }
    }

    private fun observeScanProcess() {
        viewModelScope.launch {
            try {
                val response = interactor.observeScanProcess(parameters.officeId)
                holdSplashScanner()
                observeScanProcessComplete(response)
            }catch (e:Exception){
                onTechErrorLog("observeScanProcessError", e)
                errorDialogManager.showErrorDialog(e, _navigateToDialogInfo)
            }
        }

    }

    fun onCloseDetailsClick() {
        onStartScanner()
        _navigationEvent.postValue( CourierUnloadingScanNavAction.HideUnloadingItems)
    }

    private fun observeScanProcessComplete(scanProcess: CourierUnloadingProcessData) {
        onTechEventLog("observeScanProcessComplete", "scanProcess $scanProcess")
        val scanBoxData = scanProcess.scanBoxData
        val accepted =
            resourceProvider.getAccepted(scanProcess.unloadingCounter, scanProcess.fromCounter)
        when (scanBoxData) {
            is CourierUnloadingScanBoxData.ScannerReady -> {
                _fragmentStateUI.postValue(with(scanBoxData) {
                    UnloadingFragmentState.ScannerReady(
                        UnloadingFragmentData(
                            resourceProvider.getReadyStatus(),
                            qrCode,
                            address,
                            accepted
                        )
                    )
                })
            }
            is CourierUnloadingScanBoxData.BoxAdded -> {
                _fragmentStateUI.postValue(with(scanBoxData) {
                    UnloadingFragmentState.BoxAdded(
                        UnloadingFragmentData(
                            resourceProvider.getReadyAddedBox(),
                            qrCode,
                            address,
                            accepted
                        )
                    )
                })
                _beepEvent.postValue(CourierUnloadingScanBeepState.BoxAdded)
                _completeButtonEnable.postValue(true)
            }
            CourierUnloadingScanBoxData.UnknownQr -> {
                _fragmentStateUI.postValue(UnloadingFragmentState.UnknownQr(
                    UnloadingFragmentData(
                        resourceProvider.getScanDialogTitle(),
                        resourceProvider.getUnknownQr(),
                        resourceProvider.getEmptyAddress(),
                        accepted
                    )
                ))
                _beepEvent.postValue( CourierUnloadingScanBeepState.UnknownQR)
            }
            CourierUnloadingScanBoxData.Empty -> _fragmentStateUI.postValue(
                UnloadingFragmentState.Empty(
                    UnloadingFragmentData(
                        resourceProvider.getReadyStatus(),
                        resourceProvider.getEmptyQr(),
                        resourceProvider.getEmptyAddress(),
                        accepted
                    )
                ))
            is CourierUnloadingScanBoxData.UnloadingCompleted -> {
                _fragmentStateUI.postValue( with(scanBoxData) {
                    UnloadingFragmentState.BoxAdded(
                        UnloadingFragmentData(
                            resourceProvider.getReadyAddedBox(),
                            qrCode,
                            address,
                            accepted
                        )
                    )
                })
                _completeButtonEnable.postValue( true)
            }
            is CourierUnloadingScanBoxData.ForbiddenBox -> {
                _fragmentStateUI.postValue( UnloadingFragmentState.ForbiddenBox(
                    UnloadingFragmentData(
                        resourceProvider.getReadyForbiddenBox(),
                        scanBoxData.qrCode,
                        scanBoxData.address,
                        accepted
                    )
                ))
                _beepEvent.postValue( CourierUnloadingScanBeepState.UnknownBox)
            }
            is CourierUnloadingScanBoxData.WrongBox -> {
                _fragmentStateUI.postValue( UnloadingFragmentState.WrongBox(
                    UnloadingFragmentData(
                        resourceProvider.getReadyWrongBox(),
                        scanBoxData.qrCode,
                        resourceProvider.getEmptyAddress(),
                        accepted
                    )
                ))
                _beepEvent.postValue( CourierUnloadingScanBeepState.UnknownBox)
            }
        }
    }
    private fun observeScanProgress() {
        viewModelScope.launch {
            try {
                _completeButtonEnable.postValue( when (interactor.scanLoaderProgress()) {
                    CourierUnloadingProgressData.Complete -> true
                    CourierUnloadingProgressData.Progress -> false
                })
            }catch (e:Exception){
                onTechErrorLog("observeScanProcessError", e)
            }
        }
    }

     fun onListClicked() {
        onStopScanner()
        viewModelScope.launch {
            try {
                val it = interactor.getRemainBoxes(parameters.officeId)
                if (it.isNotEmpty()) {
                    fillRemainBoxList(it)
                }
            }catch (e:Exception){

            }
        }
    }
     private fun fillRemainBoxList(boxes: List<LocalBoxEntity>) {
        val boxItems = boxes.mapIndexed(transformToRemainBoxItem).toMutableList()
        _navigationEvent.postValue( CourierUnloadingScanNavAction.InitAndShowUnloadingItems(boxItems))
    }

    private val transformToRemainBoxItem = { index: Int, localBoxEntity: LocalBoxEntity ->
        RemainBoxItem(boxName(index + 1, localBoxEntity.boxId))
    }


    private fun boxName(index: Int, boxId: String) =
        resourceProvider.getUnloadingDetails(index, boxId.takeLast(3))

    fun onCompleteUnloadClick() {
        _completeButtonEnable.postValue( false)
        onStopScanner()
        viewModelScope.launch {
            try {
                val it = interactor.getCurrentOffice(parameters.officeId)
                if (it.countBoxes == it.deliveredBoxes) {
                    confirmUnloading()
                } else {
                    showUnloadingScoreDialog(it)
                }
            }catch (e:Exception){
                onTechErrorLog("readUnloadingBoxCounterError", e)
                errorDialogManager.showErrorDialog(e, _navigateToDialogInfo)
            }
        }

    }

    private fun showUnloadingScoreDialog(office: LocalOfficeEntity) {
        _navigateToDialogConfirmScoreInfo.postValue( NavigateToDialogConfirmInfo(
            DialogInfoStyle.ERROR.ordinal,
            resourceProvider.getUnloadingDialogTitle(),
            resourceProvider.getUnloadingDialogMessage(
                office.deliveredBoxes,
                office.countBoxes
            ),
            resourceProvider.getUnloadingDialogPositive(),
            resourceProvider.getUnloadingDialogNegative()
        ))
    }

    private fun onStopScanner() {
        viewModelScope.launch {
            interactor.scannerAction(ScannerState.StopScan)
        }
    }

    private fun onStartScanner() {
        viewModelScope.launch {
            interactor.scannerAction(ScannerState.StartScan)
        }

    }

    fun onScoreDialogInfoClick() {
        onStartScanner()
    }

    fun onScoreDialogConfirmClick() {
        _completeButtonEnable.postValue( true)
        onStartScanner()
        setLoader(WaitLoader.Complete)
    }

    fun play(resId: Int) {
        playManager.play(resId)
    }

    override fun getScreenTag(): String {
        return SCREEN_TAG
    }

    companion object {
        const val SCREEN_TAG = "CourierUnloadingScan"
    }

    data class Label(val label: String)
}

/*
 private val _toolbarLabelState = MutableLiveData<Label>()
    val toolbarLabelState: LiveData<Label>
        get() = _toolbarLabelState

    private val _navigationEvent =
        SingleLiveEvent<CourierUnloadingScanNavAction>()
    val navigationEvent: LiveData<CourierUnloadingScanNavAction>
        get() = _navigationEvent

    private val _navigateToDialogInfo = SingleLiveEvent<ErrorDialogData>()
    val navigateToDialogInfo: LiveData<ErrorDialogData>
        get() = _navigateToDialogInfo

    private val _navigateToDialogScoreError = SingleLiveEvent<NavigateToDialogInfo>()
    val navigateToDialogScoreError: LiveData<NavigateToDialogInfo>
        get() = _navigateToDialogScoreError

    private val _navigateToDialogConfirmScoreInfo = SingleLiveEvent<NavigateToDialogConfirmInfo>()
    val navigateToDialogConfirmScoreInfo: LiveData<NavigateToDialogConfirmInfo>
        get() = _navigateToDialogConfirmScoreInfo

    private val _beepEvent = SingleLiveEvent<CourierUnloadingScanBeepState>()
    val beepEvent: LiveData<CourierUnloadingScanBeepState>
        get() = _beepEvent

    private val _waitLoader = SingleLiveEvent<WaitLoader>()
    val waitLoader: LiveData<WaitLoader> get() = _waitLoader

    private val _fragmentStateUI =
        MutableLiveData<UnloadingFragmentState>()
    val fragmentStateUI: LiveData<UnloadingFragmentState>
        get() = _fragmentStateUI

    private val _orderState = MutableLiveData<String>()
    val orderState: LiveData<String>
        get() = _orderState

    private val _completeButtonEnable = SingleLiveEvent<Boolean>()
    val completeButtonEnable: LiveData<Boolean>
        get() = _completeButtonEnable

    fun update() {
        initTitle()
        initToolbar()
        observeBoxInfoProcessInitState()
        observeScanProcess()
        observeScanProgress()
    }

    private fun initTitle() {
        _orderState.value = resourceProvider.getOrderId(interactor.getOrderId())
    }

    private fun holdSplashScanner() {
        interactor.scannerAction(ScannerState.StopScanWithHoldSplash)
    }

    private fun observeBoxInfoProcessInitState() {
        addSubscription(
            interactor.getCurrentOffice(parameters.officeId)
                .map { mapInitScanProcess(it) }
                .subscribe(
                    { _fragmentStateUI.value = it },
                    { onTechErrorLog("observeInitScanProcessError", it) }
                )
        )
    }

    private fun mapInitScanProcess(office: LocalOfficeEntity): UnloadingFragmentState {
        val readyStatus = resourceProvider.getReadyStatus()
        val accepted = resourceProvider.getAccepted(
            office.deliveredBoxes,
            office.countBoxes
        )
        return UnloadingFragmentState.Empty(
            UnloadingFragmentData(
                readyStatus,
                resourceProvider.getEmptyQr(),
                resourceProvider.getEmptyAddress(),
                accepted
            )
        )
    }

    private fun initToolbar() {
        addSubscription(
            interactor.getCurrentOffice(parameters.officeId)
                .subscribe({ _toolbarLabelState.value = Label(it.officeName) },
                    {})
        )
    }

    fun onCancelScoreUnloadingClick() {
        onTechEventLog("onCancelScoreUnloadingClick")
        _completeButtonEnable.value = true
        setLoader(WaitLoader.Complete)
        onStartScanner()
    }

    private fun setLoader(state: WaitLoader) {
        _waitLoader.postValue(state)
    }

    fun onDestroy() {
        clearSubscription()
    }

    fun onConfirmScoreUnloadingClick() {
        onTechEventLog("onConfirmScoreUnloadingClick")
        confirmUnloading()
    }

    private fun confirmUnloading() {
        setLoader(WaitLoader.Wait)
        addSubscription(
            interactor.completeOfficeUnload()
                .doFinally {
                    _navigationEvent.postValue(CourierUnloadingScanNavAction.NavigateToIntransit)
                    setLoader(WaitLoader.Complete)
                    clearSubscription()
                }
                .subscribe(
                    {

                    },
                    {
                        onTechErrorLog("confirmUnload", it)
                    })
        )
    }

    private fun observeScanProcess() {
        addSubscription(
            interactor.observeScanProcess(parameters.officeId)
                .doOnSubscribe { holdSplashScanner() }
                .subscribe(
                    { observeScanProcessComplete(it) },
                    {
                        onTechErrorLog("observeScanProcessError", it)
                        errorDialogManager.showErrorDialog(it, _navigateToDialogInfo)
                    }
                )
        )
    }

    fun onCloseDetailsClick() {
        onStartScanner()
        _navigationEvent.value = CourierUnloadingScanNavAction.HideUnloadingItems
    }

    private fun observeScanProcessComplete(scanProcess: CourierUnloadingProcessData) {
        onTechEventLog("observeScanProcessComplete", "scanProcess $scanProcess")
        val scanBoxData = scanProcess.scanBoxData
        val accepted =
            resourceProvider.getAccepted(scanProcess.unloadingCounter, scanProcess.fromCounter)
        when (scanBoxData) {
            is CourierUnloadingScanBoxData.ScannerReady -> {
                _fragmentStateUI.value = with(scanBoxData) {
                    UnloadingFragmentState.ScannerReady(
                        UnloadingFragmentData(
                            resourceProvider.getReadyStatus(),
                            qrCode,
                            address,
                            accepted
                        )
                    )
                }
            }
            is CourierUnloadingScanBoxData.BoxAdded -> {
                _fragmentStateUI.value = with(scanBoxData) {
                    UnloadingFragmentState.BoxAdded(
                        UnloadingFragmentData(
                            resourceProvider.getReadyAddedBox(),
                            qrCode,
                            address,
                            accepted
                        )
                    )
                }
                _beepEvent.value = CourierUnloadingScanBeepState.BoxAdded
                _completeButtonEnable.value = true
            }
            CourierUnloadingScanBoxData.UnknownQr -> {

                _fragmentStateUI.value = UnloadingFragmentState.UnknownQr(
                    UnloadingFragmentData(
                        resourceProvider.getScanDialogTitle(),
                        resourceProvider.getUnknownQr(),
                        resourceProvider.getEmptyAddress(),
                        accepted
                    )
                )
                _beepEvent.value = CourierUnloadingScanBeepState.UnknownQR
            }
            CourierUnloadingScanBoxData.Empty -> _fragmentStateUI.value =
                UnloadingFragmentState.Empty(
                    UnloadingFragmentData(
                        resourceProvider.getReadyStatus(),
                        resourceProvider.getEmptyQr(),
                        resourceProvider.getEmptyAddress(),
                        accepted
                    )
                )
            is CourierUnloadingScanBoxData.UnloadingCompleted -> {
                _fragmentStateUI.value = with(scanBoxData) {
                    UnloadingFragmentState.BoxAdded(
                        UnloadingFragmentData(
                            resourceProvider.getReadyAddedBox(),
                            qrCode,
                            address,
                            accepted
                        )
                    )
                }
                _completeButtonEnable.value = true
            }
            is CourierUnloadingScanBoxData.ForbiddenBox -> {
                _fragmentStateUI.value = UnloadingFragmentState.ForbiddenBox(
                    UnloadingFragmentData(
                        resourceProvider.getReadyForbiddenBox(),
                        scanBoxData.qrCode,
                        scanBoxData.address,
                        accepted
                    )
                )
                _beepEvent.value = CourierUnloadingScanBeepState.UnknownBox
            }
            is CourierUnloadingScanBoxData.WrongBox -> {
                _fragmentStateUI.value = UnloadingFragmentState.WrongBox(
                    UnloadingFragmentData(
                        resourceProvider.getReadyWrongBox(),
                        scanBoxData.qrCode,
                        resourceProvider.getEmptyAddress(),
                        accepted
                    )
                )
                _beepEvent.value = CourierUnloadingScanBeepState.UnknownBox
            }
        }
    }

    private fun observeScanProgress() {
        addSubscription(
            interactor.scanLoaderProgress()
                .subscribe({
                    _completeButtonEnable.value = when (it) {
                        CourierUnloadingProgressData.Complete -> true
                        CourierUnloadingProgressData.Progress -> false
                    }
                },
                    { onTechErrorLog("observeScanProcessError", it) })
        )
    }

    fun onListClicked() {
        onStopScanner()
        addSubscription(
            interactor.getRemainBoxes(parameters.officeId)
                .filter { it.isNotEmpty() }
                .subscribe(
                    { fillRemainBoxList(it) },
                    { })
        )
    }

    private fun fillRemainBoxList(boxes: List<LocalBoxEntity>) {
        val boxItems = boxes.mapIndexed(transformToRemainBoxItem).toMutableList()
        _navigationEvent.value = CourierUnloadingScanNavAction.InitAndShowUnloadingItems(boxItems)
    }

    private val transformToRemainBoxItem = { index: Int, localBoxEntity: LocalBoxEntity ->
        RemainBoxItem(boxName(index + 1, localBoxEntity.boxId))
    }


    private fun boxName(index: Int, boxId: String) =
        resourceProvider.getUnloadingDetails(index, boxId.takeLast(3))

    fun onCompleteUnloadClick() {
        _completeButtonEnable.value = false
        onStopScanner()
        addSubscription(
            interactor.getCurrentOffice(parameters.officeId)
                .flatMapCompletable {
                    if (it.countBoxes == it.deliveredBoxes) {
                        confirmUnloading()
                    } else {
                        showUnloadingScoreDialog(it)
                    }
                    Completable.complete()
                }
                .subscribe(
                    { },
                    {
                        onTechErrorLog("readUnloadingBoxCounterError", it)
                        errorDialogManager.showErrorDialog(it, _navigateToDialogInfo)
                    })
        )
    }

    private fun showUnloadingScoreDialog(office: LocalOfficeEntity) {
        _navigateToDialogConfirmScoreInfo.value = NavigateToDialogConfirmInfo(
            DialogInfoStyle.ERROR.ordinal,
            resourceProvider.getUnloadingDialogTitle(),
            resourceProvider.getUnloadingDialogMessage(
                office.deliveredBoxes,
                office.countBoxes
            ),
            resourceProvider.getUnloadingDialogPositive(),
            resourceProvider.getUnloadingDialogNegative()
        )
    }

    private fun onStopScanner() {
        interactor.scannerAction(ScannerState.StopScan)
    }

    private fun onStartScanner() {
        interactor.scannerAction(ScannerState.StartScan)
    }

    fun onScoreDialogInfoClick() {
        onStartScanner()
    }

    fun onScoreDialogConfirmClick() {
        _completeButtonEnable.value = true
        onStartScanner()
        setLoader(WaitLoader.Complete)
    }

    fun play(resId: Int) {
        playManager.play(resId)
    }

    override fun getScreenTag(): String {
        return SCREEN_TAG
    }

    companion object {
        const val SCREEN_TAG = "CourierUnloadingScan"
    }

    data class Label(val label: String)
}

 */
