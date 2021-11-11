package ru.wb.perevozka.ui.courierorderconfirm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import ru.wb.perevozka.db.entity.courierlocal.CourierOrderLocalEntity
import ru.wb.perevozka.ui.NetworkViewModel
import ru.wb.perevozka.ui.SingleLiveEvent
import ru.wb.perevozka.ui.courierorderconfirm.domain.CourierOrderConfirmInteractor
import ru.wb.perevozka.ui.dialogs.NavigateToDialogInfo
import java.text.DecimalFormat

class CourierOrderConfirmViewModel(
    compositeDisposable: CompositeDisposable,
    private val interactor: CourierOrderConfirmInteractor,
    private val resourceProvider: CourierOrderConfirmResourceProvider,
) : NetworkViewModel(compositeDisposable) {

    private val _orderInfo = MutableLiveData<CourierOrderConfirmInfoUIState>()
    val orderInfo: LiveData<CourierOrderConfirmInfoUIState>
        get() = _orderInfo

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
        initOrder()
    }

    private fun initOrder() {
        addSubscription(
            interactor.observeOrderData()
                .subscribe({ initOrderInfo(it.courierOrderLocalEntity, it.dstOffices.size) }, {})
        )
    }

    private fun initOrderInfo(courierOrderLocalEntity: CourierOrderLocalEntity, pvzCount: Int) {
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

    private fun progressComplete() {
        _progressState.value = CourierOrderConfirmProgressState.ProgressComplete
    }

    fun refuseOrderClick() {
        _navigationState.value = CourierOrderConfirmNavigationState.NavigateToBack
    }

    private fun actionProgress() = Completable.fromAction {
        _progressState.value = CourierOrderConfirmProgressState.Progress
    }

    fun confirmOrderClick() {
        addSubscription(
            actionProgress()
                .andThen(interactor.anchorTask())
                .subscribe(
                    { anchorTaskComplete() },
                    { anchorTaskError(it) })
        )
    }

    fun onChangeCarClick() {
        _navigationState.value = CourierOrderConfirmNavigationState.NavigateToChangeCar
    }

    private fun anchorTaskComplete() {
        _progressState.value = CourierOrderConfirmProgressState.ProgressComplete
        _navigationState.value = CourierOrderConfirmNavigationState.NavigateToTimer
    }

    private fun anchorTaskError(throwable: Throwable) {
        courierWarehouseError(throwable)
    }

//    fun returnToListOrderClick() {
//        _navigationState.value = CourierOrderConfirmNavigationState.NavigateToBack
//    }

    fun refuseOrderConfirmClick() {
        _navigationState.value = CourierOrderConfirmNavigationState.NavigateToBack
    }

    private fun courierWarehouseError(throwable: Throwable) {
        progressComplete()
        _navigateToDialogInfo.value = messageError(throwable, resourceProvider)
    }

    fun onCancelLoadClick() {
        clearSubscription()
    }

    data class Label(val label: String)

}