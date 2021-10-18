package ru.wb.perevozka.ui.courierloader

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import ru.wb.perevozka.app.NEED_APPROVE_COURIER_DOCUMENTS
import ru.wb.perevozka.app.NEED_SEND_COURIER_DOCUMENTS
import ru.wb.perevozka.db.AppLocalRepository
import ru.wb.perevozka.db.CourierLocalRepository
import ru.wb.perevozka.db.entity.TaskStatus
import ru.wb.perevozka.db.entity.courier.CourierWarehouseLocalEntity
import ru.wb.perevozka.db.entity.courierboxes.CourierBoxEntity
import ru.wb.perevozka.db.entity.courierlocal.CourierOrderDstOfficeLocalEntity
import ru.wb.perevozka.db.entity.courierlocal.CourierOrderLocalEntity
import ru.wb.perevozka.network.api.app.AppRemoteRepository
import ru.wb.perevozka.network.api.app.entity.CourierTaskBoxEntity
import ru.wb.perevozka.network.api.app.entity.CourierTaskMyDstOfficeEntity
import ru.wb.perevozka.network.api.app.entity.CourierTasksMyEntity
import ru.wb.perevozka.network.api.auth.AuthRemoteRepository
import ru.wb.perevozka.network.api.auth.entity.UserInfoEntity
import ru.wb.perevozka.network.exceptions.BadRequestException
import ru.wb.perevozka.network.exceptions.NoInternetException
import ru.wb.perevozka.network.rx.RxSchedulerFactory
import ru.wb.perevozka.network.token.TokenManager
import ru.wb.perevozka.network.token.UserManager
import ru.wb.perevozka.ui.NetworkViewModel
import java.util.concurrent.TimeUnit

class CourierLoaderViewModel(
    compositeDisposable: CompositeDisposable,
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val tokenManager: TokenManager,
    private val courierLocalRepository: CourierLocalRepository,
    private val appRemoteRepository: AppRemoteRepository,
    private val authRemoteRepository: AuthRemoteRepository,
    private val appLocalRepository: AppLocalRepository,
    private val userManager: UserManager
) : NetworkViewModel(compositeDisposable) {

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
        initDrawer()
        // TODO: 24.09.2021 выключить для тестирования
        //toCourierWarehouse()
        //toLoadingScanner()
        //toIntransit()

        checkUserState()

        //toCouriersCompleteRegistration("89104020582")

        //toUserForm("123456789")

        //_navigationDrawerState.value = toAgreement()
        //toUserForm("123456789")

        //clearData()
        //_navigationDrawerState.value = toPhone()
    }

    private fun initDrawer() {
        _drawerHeader.value = UserInfoEntity(tokenManager.userName(), "")
    }

    private fun checkUserState() {
        val phone = tokenManager.userPhone()
        when {
            tokenManager.resources().contains(NEED_SEND_COURIER_DOCUMENTS) -> toUserForm(phone)
            tokenManager.resources().contains(NEED_APPROVE_COURIER_DOCUMENTS) ->
                toCouriersCompleteRegistration(phone)
            else -> {
//                val error = Completable.error(NoInternetException("No Internet"))
                val timer = Completable.timer(1000, TimeUnit.MILLISECONDS)
                val taskMy = appRemoteRepository.tasksMy().map { it }
                val localTaskId =
                    courierLocalRepository.orderData().map { it.courierOrderLocalEntity.id }
                        .onErrorReturn { -1 }
                val zipData = Single.zip(taskMy, localTaskId,
                    { remoteTask, localTaskId -> tasksMyComplete(remoteTask, localTaskId) })
                    .flatMap { it }
                addSubscription(
                    timer.andThen(zipData)
                        .compose(rxSchedulerFactory.applySingleSchedulers())
                        .subscribe({ navigateTo(it) }, { taskMyError(it) })
                )
            }
        }
    }

    private fun navigateTo(navigationState: CourierLoaderNavigationState) {
        _state.value = CourierLoaderUIState.Complete
        _navigationDrawerState.value = navigationState
    }

    private fun tasksMyComplete(
        courierTasksMyEntity: CourierTasksMyEntity,
        localTaskId: Int
    ): Single<CourierLoaderNavigationState> {
        val remoteTaskId = courierTasksMyEntity.id
        return if (remoteTaskId == localTaskId) {
            saveWarehouseAndOrderAndOffices(courierTasksMyEntity)
        } else {
            clearData()
            saveWarehouseAndOrderAndOffices(courierTasksMyEntity)
                .andThen(
                    if (courierTasksMyEntity.status != TaskStatus.TIMER.status)
                        syncBoxesAndVisitedOffice(
                            remoteTaskId.toString(),
                            courierTasksMyEntity.dstOffices
                        )
                    else Completable.complete()
                )
        }
            .doOnComplete { userManager.saveStatusTask(courierTasksMyEntity.status) }
            .andThen(Single.just(getNavigationState(courierTasksMyEntity.status)))
    }

    fun syncBoxesAndVisitedOffice(taskId: String, dstOffices: List<CourierTaskMyDstOfficeEntity>) =
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

    private fun saveWarehouseAndOrderAndOffices(courierTasksMyEntity: CourierTasksMyEntity): Completable {
        val courierWarehouseLocalEntity = courierWarehouseLocalEntity(courierTasksMyEntity)
        val courierOrderLocalEntity = courierOrderLocalEntity(courierTasksMyEntity)
        val courierDstOfficesEntity = courierDstOffices(courierTasksMyEntity)
        courierLocalRepository.deleteAllWarehouse()
        courierLocalRepository.deleteAllOrder()
        courierLocalRepository.deleteAllOrderOffices()
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
        when (throwable) {
            is NullPointerException -> {
                clearData()
                _state.value = CourierLoaderUIState.Complete
                _navigationDrawerState.value = toCourierWarehouse()
            }
            is NoInternetException -> {
                val localStatus = userManager.statusTask()
                if (localStatus.isNotEmpty()) {
                    _state.value = CourierLoaderUIState.Complete
                    _navigationDrawerState.value = getNavigationState(localStatus)
                } else {
                    errorState("Интернет-соединение отсутствует")
                }
            }
            is BadRequestException -> {
                errorState(throwable.message.toString())
            }
            else -> {
                errorState("Сервис временно недоступен")
            }
        }
    }

    private fun clearData() {
        appLocalRepository.clearAll()
        userManager.clearStatus()
    }

    private fun toUserForm(phone: String) {
        _state.value = CourierLoaderUIState.Complete
        _navigationDrawerState.value = CourierLoaderNavigationState.NavigateToCourierUserForm(phone)
    }

    private fun toCouriersCompleteRegistration(phone: String) {
        _state.value = CourierLoaderUIState.Complete
        _navigationDrawerState.value =
            CourierLoaderNavigationState.NavigateToCouriersCompleteRegistration(phone)
    }

    private fun toCourierWarehouse() = CourierLoaderNavigationState.NavigateToCourierWarehouse

    private fun toPhone() = CourierLoaderNavigationState.NavigateToPhone

    private fun toTimer() = CourierLoaderNavigationState.NavigateToTimer

    private fun toLoadingScanner() = CourierLoaderNavigationState.NavigateToScanner

    private fun toIntransit() = CourierLoaderNavigationState.NavigateToIntransit

    private fun toAgreement() = CourierLoaderNavigationState.NavigateToAgreement

    private fun errorState(message: String) {
        _state.value = CourierLoaderUIState.Error(message)
    }

    fun update() {
        _state.value = CourierLoaderUIState.Progress
        checkUserState()
    }

}