package ru.wb.go.ui.courierorderconfirm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import ru.wb.go.db.entity.courierlocal.CourierOrderLocalEntity
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.ui.NetworkViewModel
import ru.wb.go.ui.SingleLiveEvent
import ru.wb.go.ui.courierorderconfirm.domain.CourierOrderConfirmInteractor
import ru.wb.go.ui.dialogs.NavigateToDialogInfo
import ru.wb.go.utils.analytics.YandexMetricManager
import ru.wb.go.utils.managers.DeviceManager
import java.text.DecimalFormat

class CourierOrderConfirmViewModel(
    compositeDisposable: CompositeDisposable,
    metric: YandexMetricManager,
    private val interactor: CourierOrderConfirmInteractor,
    private val resourceProvider: CourierOrderConfirmResourceProvider,
    private val deviceManager: DeviceManager,
) : NetworkViewModel(compositeDisposable, metric) {

    private val _orderInfo = MutableLiveData<CourierOrderConfirmInfoUIState>()
    val orderInfo: LiveData<CourierOrderConfirmInfoUIState>
        get() = _orderInfo

    private val _toolbarNetworkState = MutableLiveData<NetworkState>()
    val toolbarNetworkState: LiveData<NetworkState>
        get() = _toolbarNetworkState

    private val _versionApp = MutableLiveData<String>()
    val versionApp: LiveData<String>
        get() = _versionApp

    private val _navigationState = SingleLiveEvent<CourierOrderConfirmNavigationState>()
    val navigationState: LiveData<CourierOrderConfirmNavigationState>
        get() = _navigationState

    private val _navigateToDialogInfo = SingleLiveEvent<NavigateToDialogInfo>()
    val navigateToDialogInfo: LiveData<NavigateToDialogInfo>
        get() = _navigateToDialogInfo

    private val _progressState = MutableLiveData<CourierOrderConfirmProgressState>()
    val progressState: LiveData<CourierOrderConfirmProgressState>
        get() = _progressState


    init {
        onTechEventLog("init")
        observeNetworkState()
        fetchVersionApp()
        initOrder()
    }

    private fun observeNetworkState() {
        addSubscription(
            interactor.observeNetworkConnected().subscribe({ _toolbarNetworkState.value = it }, {})
        )
    }

    private fun fetchVersionApp() {
        _versionApp.value = resourceProvider.getVersionApp(deviceManager.appVersion)
    }

    private fun initOrder() {
        addSubscription(
            interactor.observeOrderData()
                .subscribe(
                    { initOrderInfoComplete(it.courierOrderLocalEntity, it.dstOffices.size) },
                    { initOrderInfoError(it) })
        )
    }

    private fun initOrderInfoComplete(
        courierOrderLocalEntity: CourierOrderLocalEntity, pvzCount: Int
    ) {
        onTechEventLog("initOrderInfoComplete", "pvzCount: $pvzCount")
        with(courierOrderLocalEntity) {
            val decimalFormat = DecimalFormat("#,###.##")
            val coast = decimalFormat.format(minPrice)
            _orderInfo.value = CourierOrderConfirmInfoUIState.InitOrderInfo(
                order = resourceProvider.getOrder(id),
                carNumber = resourceProvider.getCarNumber(interactor.carNumber()),
                arrive = resourceProvider.getArrive(reservedDuration),
                pvz = resourceProvider.getPvz(pvzCount),
                volume = resourceProvider.getVolume(minBoxesCount, minVolume),
                coast = resourceProvider.getCoast(coast)
            )
        }
    }

    private fun initOrderInfoError(throwable: Throwable) {
        onTechErrorLog("initOrderInfoError", throwable)
    }

    private fun progressComplete() {
        _progressState.value = CourierOrderConfirmProgressState.ProgressComplete
    }

    fun onRefuseOrderClick() {
        onTechEventLog("onRefuseOrderClick")
        _navigationState.value = CourierOrderConfirmNavigationState.NavigateToBack
    }

    private fun actionProgress() = Completable.fromAction {
        _progressState.value = CourierOrderConfirmProgressState.Progress
    }

    fun onConfirmOrderClick() {
        onTechEventLog("onConfirmOrderClick")
        addSubscription(
            actionProgress()
                .andThen(interactor.anchorTask())
                .subscribe(
                    { anchorTaskComplete() },
                    { anchorTaskError(it) })
        )
    }

    fun onChangeCarClick() {
        onTechEventLog("onChangeCarClick")
        _navigationState.value = CourierOrderConfirmNavigationState.NavigateToChangeCar
    }

    private fun anchorTaskComplete() {
        onTechEventLog("anchorTaskComplete", "NavigateToTimer")
        _progressState.value = CourierOrderConfirmProgressState.ProgressComplete
        _navigationState.value = CourierOrderConfirmNavigationState.NavigateToTimer
    }

    private fun anchorTaskError(throwable: Throwable) {
        onTechErrorLog("anchorTaskError", throwable)
        courierWarehouseError(throwable)
    }

    fun refuseOrderConfirmClick() {
        _navigationState.value = CourierOrderConfirmNavigationState.NavigateToBack
    }

    private fun courierWarehouseError(throwable: Throwable) {
        progressComplete()
        _navigateToDialogInfo.value = messageError(throwable, resourceProvider)
    }

    fun onCancelLoadClick() {
        onTechEventLog("onCancelLoadClick")
        clearSubscription()
    }

    override fun getScreenTag(): String {
        return SCREEN_TAG
    }

    companion object {
        const val SCREEN_TAG = "CourierUnloadingBoxes"
    }

}