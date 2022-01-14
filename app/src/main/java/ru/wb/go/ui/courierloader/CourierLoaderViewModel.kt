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
import ru.wb.go.network.token.UserManager
import ru.wb.go.ui.NetworkViewModel
import ru.wb.go.utils.analytics.YandexMetricManager
import ru.wb.go.utils.managers.ConfigManager
import ru.wb.go.utils.managers.DeviceManager

class CourierLoaderViewModel(
    compositeDisposable: CompositeDisposable,
    metric: YandexMetricManager,
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val tokenManager: TokenManager,
    private val locRepo: CourierLocalRepository,
    private val remoteRepo: AppRemoteRepository,
    private val userManager: UserManager,
    private val deviceManager: DeviceManager,
    private val configManager: ConfigManager,
    private val resourceProvider: CourierLoaderResourceProvider,
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
    }

    private fun initDrawer() {
        _drawerHeader.value = UserInfoEntity(tokenManager.userName(), tokenManager.userCompany())
    }

    private fun initVersion() {
        addSubscription(
            remoteRepo.appVersion()
                .doOnSuccess { saveAppVersion(it) }
                .compose(rxSchedulerFactory.applySingleSchedulers())
                .subscribe(
                    { appVersionUpdateComplete(it) },
                    { appVersionUpdateError(it) })
        )
    }

    private fun saveAppVersion(it: String) {
        val matchVersion = it.replace(Regex("[^\\d.]"), "")
        configManager.saveAppVersion(matchVersion)
    }

    private fun appVersionUpdateComplete(version: String) {
        onTechEventLog("appVersionUpdateComplete", "appStart $version")
        appStart()
    }

    private fun appVersionUpdateError(throwable: Throwable) {
        onTechErrorLog("appVersionUpdateError", throwable)
        appStart()
    }

    private fun appStart() {
        val appVersion = versionCodeToInt(configManager.readAppVersion())
        if (isVersionActual(appVersion)) loadApp()
        else toAppUpdate()
    }

    private fun loadApp() {
        onTechEventLog("loadApp", "checkUserState")

        checkUserState()

    }

    private fun isVersionActual(remotes: Int): Boolean {
        return versionCodeToInt(deviceManager.appVersion) >= remotes
    }

    private fun versionCodeToInt(code: String): Int {
        return code.replace("\\D+".toRegex(), "").toInt()
    }

    private fun checkUserState() {
        val phone = tokenManager.userPhone()
        when {
            tokenManager.resources().contains(NEED_SEND_COURIER_DOCUMENTS) -> toNewRegistration(
                phone
            )
            tokenManager.resources().contains(NEED_CORRECT_COURIER_DOCUMENTS) -> {
                toUserForm(phone)
            }
            tokenManager.resources().contains(NEED_APPROVE_COURIER_DOCUMENTS) ->
                toCouriersCompleteRegistration(phone)
            else -> {
                val order = locRepo.getOrder()
                val taskMy = remoteRepo.tasksMy(order?.orderId)

                addSubscription(
                    taskMy
                        .flatMap {
                            solveJobInitialState(
                                it,
                                order
                            )
                        }
                        .compose(rxSchedulerFactory.applySingleSchedulers())
                        .subscribe({ taskMyComplete(it) }, { taskMyError(it) })


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
            (order == null || remoteTaskId != order.orderId) && remoteTaskId != -2 ->
                syncFromServer(remoteOrder)
                    .andThen(Single.just(getNavigationState(remoteOrder.order.status)))
            else -> {
                val localStatus = order!!.status
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
            .flatMapCompletable{
                locRepo.saveRemoteOrder(remoteOrder, it)
            }

    }

    private fun getNavigationState(status: String) =
        when (status) {
            TaskStatus.TIMER.status ->  toTimer()
            TaskStatus.STARTED.status -> toLoadingScanner()
            TaskStatus.INTRANSIT.status -> toIntransit()
            else -> toCourierWarehouse()
        }


    private fun taskMyError(throwable: Throwable) {
        onTechErrorLog("taskMyError", throwable)
        when (throwable) {
            is NullPointerException -> {
                clearCurrentLocalData()
                _state.value = CourierLoaderUIState.Complete
                _navigationDrawerState.value = toCourierWarehouse()
            }

            is BadRequestException -> {
                errorState(throwable.message.toString())
            }
            else -> {
                errorState(throwable.toString())
            }
        }
    }

    private fun clearCurrentLocalData() {
        //FIXME Clear local repo
        locRepo.clearOrder()

    }

    private fun toUserForm(phone: String) {
        onTechEventLog("toUserForm")
        addSubscription(
            remoteRepo.getCourierDocuments()
                .compose(rxSchedulerFactory.applySingleSchedulers())
                .subscribe({
                    _state.value = CourierLoaderUIState.Complete
                    _navigationDrawerState.value =
                        CourierLoaderNavigationState.NavigateToCourierUserForm(phone, it)

                }, { taskMyError(it) })
        )
    }

    private fun toNewRegistration(phone: String) {
        onTechEventLog("toNewRegistration")
        val docs = CourierDocumentsEntity()
        _state.value = CourierLoaderUIState.Complete
        _navigationDrawerState.value =
            CourierLoaderNavigationState.NavigateToCourierUserForm(phone, docs)
    }

    private fun toCouriersCompleteRegistration(phone: String) {
        onTechEventLog("toCouriersCompleteRegistration")
        _state.value = CourierLoaderUIState.Complete
        _navigationDrawerState.value =
            CourierLoaderNavigationState.NavigateToCouriersCompleteRegistration(phone)
    }

    private fun toCourierWarehouse() = CourierLoaderNavigationState.NavigateToCourierWarehouse

    private fun toTimer() = CourierLoaderNavigationState.NavigateToTimer

    private fun toLoadingScanner() = CourierLoaderNavigationState.NavigateToScanner

    private fun toIntransit() = CourierLoaderNavigationState.NavigateToIntransit

    private fun toAppUpdate() {
        onTechEventLog("toAppUpdate", "NavigateToAppUpdate")
        _navigationDrawerState.value = CourierLoaderNavigationState.NavigateToAppUpdate
    }

    private fun errorState(message: String) {
        onTechEventLog("errorState", "message")
        _state.value = CourierLoaderUIState.Error(message)
    }

    fun update() {
        onTechEventLog("update")
        _state.value = CourierLoaderUIState.Progress
        checkUserState()
    }

    override fun getScreenTag(): String {
        return SCREEN_TAG
    }

    companion object {
        const val SCREEN_TAG = "CourierLoader"
    }

}