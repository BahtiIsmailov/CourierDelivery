package ru.wb.go.ui.courierloader

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import ru.wb.go.app.NEED_APPROVE_COURIER_DOCUMENTS
import ru.wb.go.app.NEED_CORRECT_COURIER_DOCUMENTS
import ru.wb.go.app.NEED_SEND_COURIER_DOCUMENTS
import ru.wb.go.db.CourierLocalRepository
import ru.wb.go.db.entity.TaskStatus
import ru.wb.go.db.entity.courier.CourierWarehouseLocalEntity
import ru.wb.go.db.entity.courierboxes.CourierBoxEntity
import ru.wb.go.db.entity.courierlocal.CourierOrderDstOfficeLocalEntity
import ru.wb.go.db.entity.courierlocal.CourierOrderLocalEntity
import ru.wb.go.network.api.app.AppRemoteRepository
import ru.wb.go.network.api.app.entity.CourierDocumentsEntity
import ru.wb.go.network.api.app.entity.CourierTaskBoxEntity
import ru.wb.go.network.api.app.entity.CourierTaskMyDstOfficeEntity
import ru.wb.go.network.api.app.entity.CourierTasksMyEntity
import ru.wb.go.network.api.auth.entity.UserInfoEntity
import ru.wb.go.network.exceptions.BadRequestException
import ru.wb.go.network.exceptions.NoInternetException
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
    private val courierLocalRepository: CourierLocalRepository,
    private val appRemoteRepository: AppRemoteRepository,
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
            appRemoteRepository.appVersion()
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
        onTechEventLog("appVersionUpdateComplete", "appStart")
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
                val taskMy = appRemoteRepository.tasksMy()
                val localTask = courierLocalRepository.orderData()
//                        .onErrorReturn {
//                            CourierOrderLocalDataEntity(
//                                courierOrderLocalEntity = CourierOrderLocalEntity(
//                                    id = -1,
//                                    routeID = 0,
//                                    gate = "",
//                                    minPrice = 0,
//                                    minVolume = 0,
//                                    minBoxesCount = 0,
//                                    reservedDuration = "",
//                                    reservedAt = ""
//                                ), dstOffices = listOf()
//                            )
//                        }
//                        .map { it.courierOrderLocalEntity }
//                val zipData = Single.zip(taskMy, localTask,
//                    { remoteTask, lT ->
//                        solveJobInitialState(remoteTask, lT)
//                    })
//                    .map { it }

//                addSubscription(
//                    zipData
//                        .compose(rxSchedulerFactory.applySingleSchedulers())
//                        .subscribe({ taskMyComplete(it) }, { taskMyError(it) })
//                )
                addSubscription(
                    taskMy
                        .flatMap {
                            solveJobInitialState(
                                it,
                                localTask?.courierOrderLocalEntity
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
        courierTasksMyEntity: CourierTasksMyEntity,
        localTask: CourierOrderLocalEntity?
    ): Single<CourierLoaderNavigationState> {
        val remoteTaskId = courierTasksMyEntity.id
        onTechEventLog(
            "tasksMyComplete",
            "remoteTaskId: $remoteTaskId localTaskId: ${localTask?.id ?: -1}"
        )
        // if remote fail do it local(if local exist) else fail
        // sync remote
        // if remote!=local - sync
        // if remote==local -not sync? but take reserve

        return when {
            localTask == null || remoteTaskId != localTask.id ->
                syncFromServer(courierTasksMyEntity, remoteTaskId)
            else -> Completable.complete()
        }
            .andThen(Single.just(getNavigationState(courierTasksMyEntity.status)))
    }

    private fun syncFromServer(
        courierTasksMyEntity: CourierTasksMyEntity,
        remoteTaskId: Int
    ): Completable {
        onTechEventLog(
            "syncWarehouseAndBoxes",
            "clearData and saveWarehouseAndOrderAndOfficesAndCost"
        )
        clearCurrentLocalData()

        if (remoteTaskId == 0) {
            return Completable.complete()
        }

        return saveRemoteData(courierTasksMyEntity)
            .andThen(
                syncBoxesAndVisitedOffice(
                    remoteTaskId.toString(),
                    courierTasksMyEntity.dstOffices
                )
            )
    }

    private fun syncBoxesAndVisitedOffice(
        taskId: String,
        dstOffices: List<CourierTaskMyDstOfficeEntity>
    ) =
        appRemoteRepository.taskBoxes(taskId)
            .map { it.data }
            .flatMap { taskBoxes -> convertTaskBoxes(taskBoxes, dstOffices) }
            .flatMapCompletable { courierLocalRepository.saveLoadingBoxes(it) }
            .andThen(courierLocalRepository.updateVisitedOfficeByBoxes())

    private fun convertTaskBoxes(
        taskBoxes: List<CourierTaskBoxEntity>,
        dstOffices: List<CourierTaskMyDstOfficeEntity>
    ) = Observable.fromIterable(taskBoxes)
        .map { taskBox ->
            val address = dstOffices.find { it.id == taskBox.dstOfficeID }?.fullAddress ?: ""
            CourierBoxEntity(
                id = taskBox.id,
                address = address,
                dstOfficeId = taskBox.dstOfficeID,
                loadingAt = taskBox.loadingAt,
                deliveredAt = taskBox.deliveredAt
            )
        }.toList()

    private fun saveRemoteData(courierTasksMyEntity: CourierTasksMyEntity): Completable {
        val courierWarehouseLocalEntity = courierWarehouseLocalEntity(courierTasksMyEntity)
        val courierOrderLocalEntity = courierOrderLocalEntity(courierTasksMyEntity)
        val courierDstOfficesEntity = courierDstOffices(courierTasksMyEntity)
        userManager.saveCostTask(courierTasksMyEntity.cost)
        userManager.saveStatusTask(courierTasksMyEntity.status)
        return courierLocalRepository.saveWarehouseAndOrderAndOffices(
            courierWarehouseLocalEntity, courierOrderLocalEntity, courierDstOfficesEntity
        )
    }

    private fun getNavigationState(status: String) =
        when (status) {
            TaskStatus.TIMER.status -> toTimer()
            TaskStatus.STARTED.status -> toLoadingScanner()
            TaskStatus.INTRANSIT.status -> toIntransit()
            else -> toCourierWarehouse()
        }

    private fun courierDstOffices(courierTasksMyEntity: CourierTasksMyEntity): MutableList<CourierOrderDstOfficeLocalEntity> {
        val courierOrderDstOfficesLocalEntity = mutableListOf<CourierOrderDstOfficeLocalEntity>()
        courierTasksMyEntity.dstOffices.forEach {
            with(it) {
                courierOrderDstOfficesLocalEntity.add(
                    CourierOrderDstOfficeLocalEntity(
                        id = id,
                        orderId = courierTasksMyEntity.id,
                        name = name,
                        fullAddress = fullAddress,
                        longitude = long,
                        latitude = lat,
                        visitedAt = ""
                    )
                )
            }
        }
        return courierOrderDstOfficesLocalEntity
    }

    private fun courierWarehouseLocalEntity(courierTasksMyEntity: CourierTasksMyEntity): CourierWarehouseLocalEntity {
        return with(courierTasksMyEntity.srcOffice) {
            CourierWarehouseLocalEntity(
                id = id,
                name = name,
                fullAddress = fullAddress,
                longitude = long,
                latitude = lat
            )
        }
    }

    private fun courierOrderLocalEntity(courierTasksMyEntity: CourierTasksMyEntity): CourierOrderLocalEntity {
        //        val courierOrderSrcOfficesLocalEntity = with(courierTasksMyEntity.srcOffice) {
//            CourierOrderSrcOfficeLocalEntity(
//                id = id,
//                name = name,
//                fullAddress = fullAddress,
//                longitude = long,
//                latitude = lat
//            )
//        }
        val courierOrderLocalEntity = with(courierTasksMyEntity) {
            CourierOrderLocalEntity(
                id = id,
                routeID = routeID,
                gate = gate,
                //                srcOffice = courierOrderSrcOfficesLocalEntity,
                minPrice = minPrice,
                minVolume = minVolume,
                minBoxesCount = minBoxesCount,
                reservedDuration = reservedDuration,
                reservedAt = reservedAt,
            )
        }
        return courierOrderLocalEntity
    }

    private fun taskMyError(throwable: Throwable) {
        onTechErrorLog("taskMyError", throwable)
        when (throwable) {
            is NullPointerException -> {
                clearCurrentLocalData()
                _state.value = CourierLoaderUIState.Complete
                _navigationDrawerState.value = toCourierWarehouse()
            }
            is NoInternetException -> {
                val localStatus = userManager.statusTask()
                if (localStatus.isNotEmpty()) {
                    _state.value = CourierLoaderUIState.Complete
                    _navigationDrawerState.value = getNavigationState(localStatus)
                } else {
                    errorState(resourceProvider.getGenericInternetTitleError())
                }
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
        courierLocalRepository.clearOrder()
        userManager.clearStatus()
    }

    private fun toUserForm(phone: String) {
        onTechEventLog("toUserForm")
        addSubscription(
            appRemoteRepository.getCourierDocuments()
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