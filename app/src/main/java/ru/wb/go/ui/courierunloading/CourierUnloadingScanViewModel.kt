package ru.wb.go.ui.courierunloading

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import ru.wb.go.db.entity.courierlocal.LocalOfficeEntity
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.ui.NetworkViewModel
import ru.wb.go.ui.SingleLiveEvent
import ru.wb.go.ui.courierunloading.domain.*
import ru.wb.go.ui.dialogs.DialogInfoStyle
import ru.wb.go.ui.dialogs.NavigateToDialogConfirmInfo
import ru.wb.go.ui.dialogs.NavigateToDialogInfo
import ru.wb.go.ui.scanner.domain.ScannerState
import ru.wb.go.utils.WaitLoader
import ru.wb.go.utils.analytics.YandexMetricManager
import ru.wb.go.utils.managers.DeviceManager
import ru.wb.go.utils.managers.ErrorDialogData
import ru.wb.go.utils.managers.ErrorDialogManager
import ru.wb.go.utils.managers.PlayManager

class CourierUnloadingScanViewModel(
    private val parameters: CourierUnloadingScanParameters,
    compositeDisposable: CompositeDisposable,
    metric: YandexMetricManager,
    private val resourceProvider: CourierUnloadingResourceProvider,
    private val interactor: CourierUnloadingInteractor,
    private val deviceManager: DeviceManager,
    private val errorDialogManager: ErrorDialogManager,
    private val playManager: PlayManager,
) : NetworkViewModel(compositeDisposable, metric) {
    private val _toolbarLabelState = MutableLiveData<Label>()
    val toolbarLabelState: LiveData<Label>
        get() = _toolbarLabelState

    private val _navigationEvent =
        SingleLiveEvent<CourierUnloadingScanNavAction>()
    val navigationEvent: LiveData<CourierUnloadingScanNavAction>
        get() = _navigationEvent

    private val _toolbarNetworkState = MutableLiveData<NetworkState>()
    val toolbarNetworkState: LiveData<NetworkState>
        get() = _toolbarNetworkState

    private val _versionApp = MutableLiveData<String>()
    val versionApp: LiveData<String>
        get() = _versionApp

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

    private val _waitLoader =
        SingleLiveEvent<WaitLoader>()
    val waitLoader: LiveData<WaitLoader>
        get() = _waitLoader

    private val _fragmentStateUI =
        MutableLiveData<UnloadingFragmentState>()
    val fragmentStateUI: LiveData<UnloadingFragmentState>
        get() = _fragmentStateUI

    private val _completeButtonEnable = SingleLiveEvent<Boolean>()
    val completeButtonEnable: LiveData<Boolean>
        get() = _completeButtonEnable

    init {
        initToolbar()
        fetchVersionApp()
        observeNetworkState()
        observeBoxInfoProcessInitState()
        observeScanProcess()
        observeScanProgress()
    }

    private fun fetchVersionApp() {
        _versionApp.value = resourceProvider.getVersionApp(deviceManager.toolbarVersion)
    }

    private fun observeBoxInfoProcessInitState() {
        addSubscription(
            interactor.getCurrentOffice(parameters.officeId)
                .map { mapInitScanProcess(it) }
                .subscribe(
                    {
                        _fragmentStateUI.value = it
                    },
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

    private fun observeNetworkState() {
        addSubscription(
            interactor.observeNetworkConnected()
                .subscribe({ _toolbarNetworkState.value = it }, {})
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

    fun onConfirmScoreUnloadingClick() {
        onTechEventLog("onConfirmScoreUnloadingClick")
        confirmUnloading()
    }

    private fun confirmUnloading() {
        setLoader(WaitLoader.Wait)
        addSubscription(
            interactor.completeOfficeUnload()
                .doFinally {
                    setLoader(WaitLoader.Complete)
                    clearSubscription()
                    _navigationEvent.postValue(CourierUnloadingScanNavAction.NavigateToIntransit)
                }
                .subscribe(
                    { },
                    {
                        onTechErrorLog("confirmUnload", it)
                    })
        )
    }

    private fun observeScanProcess() {
        addSubscription(
            interactor.observeScanProcess(parameters.officeId)
                .subscribe(
                    { observeScanProcessComplete(it) },
                    {
                        onTechErrorLog("observeScanProcessError", it)
                        errorDialogManager.showErrorDialog(it, _navigateToDialogInfo)
                    }
                )
        )
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
        _navigationEvent.value =
            CourierUnloadingScanNavAction.NavigateToBoxes(officeId = parameters.officeId)
    }

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
                    {
                        setLoader(WaitLoader.Complete)
                    },
                    {
                        onTechErrorLog("readUnloadingBoxCounterError", it)
                        setLoader(WaitLoader.Complete)
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

    fun onStopScanner() {
        interactor.scannerAction(ScannerState.StopScan)
    }

    fun onStartScanner() {
        interactor.scannerAction(ScannerState.Start)
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
