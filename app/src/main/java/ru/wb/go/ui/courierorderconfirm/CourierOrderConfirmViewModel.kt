package ru.wb.go.ui.courierorderconfirm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable
import ru.wb.go.db.entity.courierlocal.CourierOrderLocalEntity
import ru.wb.go.network.exceptions.CustomException
import ru.wb.go.network.exceptions.HttpObjectNotFoundException
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.ui.NetworkViewModel
import ru.wb.go.ui.SingleLiveEvent
import ru.wb.go.ui.courierorderconfirm.domain.CourierOrderConfirmInteractor
import ru.wb.go.ui.dialogs.DialogInfoFragment
import ru.wb.go.utils.analytics.YandexMetricManager
import ru.wb.go.utils.managers.DeviceManager
import ru.wb.go.utils.managers.ErrorDialogData
import ru.wb.go.utils.managers.ErrorDialogManager
import java.text.DecimalFormat

class CourierOrderConfirmViewModel(
    compositeDisposable: CompositeDisposable,
    metric: YandexMetricManager,
    private val interactor: CourierOrderConfirmInteractor,
    private val resourceProvider: CourierOrderConfirmResourceProvider,
    private val deviceManager: DeviceManager,
    private val errorDialogManager: ErrorDialogManager,
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

    private val _navigateToDialogInfo = SingleLiveEvent<ErrorDialogData>()
    val navigateToDialogInfo: LiveData<ErrorDialogData>
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
                .subscribe {
                    initOrderInfoComplete(it.courierOrderLocalEntity, it.dstOffices.size)
                }
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


    private fun setLoader(state: CourierOrderConfirmProgressState) {
        _progressState.value = state
    }

    fun onRefuseOrderClick() {
        onTechEventLog("onRefuseOrderClick")
        _navigationState.value = CourierOrderConfirmNavigationState.NavigateToBack
    }

    fun goToWarehouse() {
        _navigationState.value = CourierOrderConfirmNavigationState.NavigateToWarehouse
    }

    fun onConfirmOrderClick() {
        onTechEventLog("onConfirmOrderClick")
        setLoader(CourierOrderConfirmProgressState.Progress)
        addSubscription(
            interactor.anchorTask()
                .subscribe(
                    {
                        anchorTaskComplete()
                    },
                    {
                        onTechErrorLog("anchorTaskError", it)
                        setLoader(CourierOrderConfirmProgressState.ProgressComplete)
                        if(it is HttpObjectNotFoundException){
                            val ex = CustomException("Заказ уже в работе. Выберите другой заказ.")
                            errorDialogManager.showErrorDialog(ex, _navigateToDialogInfo)
                        }else {
                            errorDialogManager.showErrorDialog(it, _navigateToDialogInfo, DialogInfoFragment.DIALOG_INFO2_TAG)
                        }
                    })
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

    override fun getScreenTag(): String {
        return SCREEN_TAG
    }

    companion object {
        const val SCREEN_TAG = "CourierOrderConfirm"
    }

}