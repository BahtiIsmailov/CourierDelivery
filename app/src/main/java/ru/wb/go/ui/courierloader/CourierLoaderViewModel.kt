package ru.wb.go.ui.courierloader

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import ru.wb.go.app.NEED_APPROVE_COURIER_DOCUMENTS
import ru.wb.go.app.NEED_CORRECT_COURIER_DOCUMENTS
import ru.wb.go.app.NEED_SEND_COURIER_DOCUMENTS
import ru.wb.go.db.CourierLocalRepository
import ru.wb.go.db.entity.TaskStatus
import ru.wb.go.db.entity.courierlocal.LocalComplexOrderEntity
import ru.wb.go.db.entity.courierlocal.LocalOrderEntity
import ru.wb.go.network.api.app.AppRemoteRepository
import ru.wb.go.network.api.app.entity.CourierDocumentsEntity
import ru.wb.go.network.api.auth.entity.UserInfoEntity
import ru.wb.go.network.exceptions.BadRequestException
import ru.wb.go.network.rx.RxSchedulerFactory
import ru.wb.go.network.token.TokenManager
import ru.wb.go.ui.NetworkViewModel
import ru.wb.go.ui.courierdata.CourierDataParameters
import ru.wb.go.utils.analytics.YandexMetricManager
import ru.wb.go.utils.managers.DeviceManager
import ru.wb.go.utils.managers.SettingsManager

class CourierLoaderViewModel(
    compositeDisposable: CompositeDisposable,
    metric: YandexMetricManager,
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val tokenManager: TokenManager,
    private val locRepo: CourierLocalRepository,
    private val remoteRepo: AppRemoteRepository,
    private val deviceManager: DeviceManager,
    private val resourceProvider: CourierLoaderResourceProvider,
    private val settingsManager: SettingsManager,
) : NetworkViewModel(compositeDisposable, metric) {

    private val _drawerHeader = MutableLiveData<UserInfoEntity>()
    val drawerHeader: LiveData<UserInfoEntity>
        get() = _drawerHeader

    private val _navigationDrawerState = MutableLiveData<CourierLoaderNavigationState>()
    val navigationDrawerState: LiveData<CourierLoaderNavigationState>
        get() = _navigationDrawerState

    private val _state = MutableLiveData<CourierLoaderUIState>()
    val state: LiveData<CourierLoaderUIState>
        get() = _state


    init {
        onTechEventLog("init")
        initDrawer()
        initVersion()
        checkRootState()
    }

    private fun initDrawer() {
        _drawerHeader.value = UserInfoEntity(tokenManager.userName(), tokenManager.userCompany())
    }

    fun initVersion() {

        _state.value = CourierLoaderUIState.Progress

        addSubscription(
            remoteRepo.appVersion()
                .compose(rxSchedulerFactory.applySingleSchedulers())
                .subscribe(
                    { appVersionUpdateComplete(it) },
                    { appVersionUpdateError(it) })
        )
    }


    private fun appVersionUpdateComplete(version: String) {
        onTechEventLog("appVersionUpdateComplete", "appStart $version")
        checkUserState(version)
    }

    private fun appVersionUpdateError(throwable: Throwable) {
        onTechErrorLog("appVersionUpdateError", throwable)
        checkUserState("0.0.0")
    }

    private fun goToUpdate(version: String): Boolean {
        onTechEventLog("admin version: $version")
        val res = !deviceManager.isAppVersionActual(version)
        if (res)
            toAppUpdate()
        return res
    }

    private fun checkRootState() {
        if (resourceProvider.isRooted()) {
            onTechEventLog("phoneIsRooted")
        }
    }

    private fun checkUserState(version: String) {
        val phone = tokenManager.userPhone()

        when {
            tokenManager.resources().contains(NEED_SEND_COURIER_DOCUMENTS) -> {
                if (!goToUpdate(version))
                    toNewRegistration(phone)
            }
            tokenManager.resources().contains(NEED_CORRECT_COURIER_DOCUMENTS) -> {
                if (!goToUpdate(version))
                    toUserForm(phone)
            }
            tokenManager.resources().contains(NEED_APPROVE_COURIER_DOCUMENTS) -> {
                if (!goToUpdate(version))
                    toCouriersCompleteRegistration(phone)
            }
            else -> {
                checkNewInstallation()
                val order = locRepo.getOrder()
                val taskMy =
                    remoteRepo.tasksMy(order?.orderId)
                        .flatMap { solveJobInitialState(it, order) }
                if (order == null && goToUpdate(version)) {
                    return
                }
                val navigation = if (tokenManager.isDemo()) Single.just(toCourierWarehouse())
                else taskMy
                addSubscription(
                    navigation
                        .compose(rxSchedulerFactory.applySingleSchedulers())
                        .subscribe({ taskMyComplete(it) }, {
                            onTechErrorLog("startNavigation", it)
                            onRxError(it)
                        })
                )
            }
        }
    }

    private fun taskMyComplete(navigationState: CourierLoaderNavigationState) {
        onTechEventLog("taskMyComplete", "navigationState $navigationState")
        _state.value = CourierLoaderUIState.Complete
        _navigationDrawerState.value = navigationState
    }

    private fun solveJobInitialState(
        remoteOrder: LocalComplexOrderEntity,
        order: LocalOrderEntity?
    ): Single<CourierLoaderNavigationState> {
        val remoteTaskId = remoteOrder.order.orderId
        return when {
            (order == null || remoteTaskId != order.orderId) && remoteTaskId != -2 -> {
                onTechEventLog("OrderSynchronization", "Get from server")
                syncFromServer(remoteOrder)
                    .andThen(Single.just(getNavigationState(remoteOrder.order.status)))
            }
            else -> {
                val localStatus = order!!.status
                onTechEventLog("OrderSynchronization", "Get local version")
                Completable.complete()
                    .andThen(Single.just(getNavigationState(localStatus)))
            }
        }

    }

    private fun syncFromServer(
        remoteOrder: LocalComplexOrderEntity,
    ): Completable {
        clearCurrentLocalData()

        assert(remoteOrder.order.orderId != -2)

        if (remoteOrder.order.orderId < 0) {
            return Completable.complete()
        }

        return remoteRepo.taskBoxes(remoteOrder.order.orderId.toString())
            .flatMapCompletable {
                locRepo.saveRemoteOrder(remoteOrder, it)
            }

    }

    private fun getNavigationState(status: String) =
        when (status) {
            TaskStatus.TIMER.status -> toTimer()
            TaskStatus.STARTED.status -> toLoadingScanner()
            TaskStatus.INTRANSIT.status -> toIntransit()
            else -> toCourierWarehouse()
        }

    private fun onRxError(throwable: Throwable) {
        when (throwable) {
            is NullPointerException -> {
                clearCurrentLocalData()
                _state.value = CourierLoaderUIState.Complete
                _navigationDrawerState.value = toCourierWarehouse()
            }

            is BadRequestException -> {
                _state.value = CourierLoaderUIState.Error(throwable.message.toString())
            }
            else -> {
                _state.value = CourierLoaderUIState.Error(throwable.toString())
            }
        }
    }

    private fun clearCurrentLocalData() {
        locRepo.clearOrder()
    }

    private fun toUserForm(phone: String) {
        addSubscription(
            remoteRepo.getCourierDocuments()
                .compose(rxSchedulerFactory.applySingleSchedulers())
                .subscribe({
                    _state.value = CourierLoaderUIState.Complete
                    _navigationDrawerState.value =
                        CourierLoaderNavigationState.NavigateToCourierUserForm(
                            CourierDataParameters(phone = phone, docs = it)
                        )

                }, {
                    onTechErrorLog("getUserDocs", it)
                    onRxError(it)
                })
        )
    }

    private fun toNewRegistration(phone: String) {
        val docs = CourierDocumentsEntity()
        _state.value = CourierLoaderUIState.Complete
        _navigationDrawerState.value =
            CourierLoaderNavigationState.NavigateToCourierUserForm(
                CourierDataParameters(
                    phone = phone,
                    docs = docs
                )
            )
    }

    private fun toCouriersCompleteRegistration(phone: String) {
        _state.value = CourierLoaderUIState.Complete
        _navigationDrawerState.value =
            CourierLoaderNavigationState.NavigateToCouriersCompleteRegistration(phone)
    }

    private fun toCourierWarehouse() = CourierLoaderNavigationState.NavigateToCourierWarehouse

    private fun toTimer() = CourierLoaderNavigationState.NavigateToTimer

    private fun toLoadingScanner() = CourierLoaderNavigationState.NavigateToScanner

    private fun toIntransit() = CourierLoaderNavigationState.NavigateToIntransit

    private fun toAppUpdate() {
        _navigationDrawerState.value = CourierLoaderNavigationState.NavigateToAppUpdate
    }

    private fun checkNewInstallation() {
        if (settingsManager.checkNewInstall(deviceManager.appVersion)) {
            onTechEventLog("newInstallDetected", deviceManager.appVersion)
        }
    }

    override fun getScreenTag(): String {
        return SCREEN_TAG
    }

    companion object {
        const val SCREEN_TAG = "CourierLoader"
    }

}