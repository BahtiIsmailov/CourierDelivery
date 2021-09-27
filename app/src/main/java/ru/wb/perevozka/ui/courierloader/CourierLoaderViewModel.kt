package ru.wb.perevozka.ui.courierloader

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable
import ru.wb.perevozka.app.NEED_APPROVE_COURIER_DOCUMENTS
import ru.wb.perevozka.app.NEED_SEND_COURIER_DOCUMENTS
import ru.wb.perevozka.db.CourierLocalRepository
import ru.wb.perevozka.db.entity.courierlocal.CourierOrderDstOfficeLocalEntity
import ru.wb.perevozka.db.entity.courierlocal.CourierOrderLocalEntity
import ru.wb.perevozka.network.api.app.AppRemoteRepository
import ru.wb.perevozka.network.api.app.entity.CourierTasksMyEntity
import ru.wb.perevozka.network.api.auth.AuthRemoteRepository
import ru.wb.perevozka.network.api.auth.entity.UserInfoEntity
import ru.wb.perevozka.network.exceptions.BadRequestException
import ru.wb.perevozka.network.exceptions.NoInternetException
import ru.wb.perevozka.network.rx.RxSchedulerFactory
import ru.wb.perevozka.network.token.TokenManager
import ru.wb.perevozka.ui.NetworkViewModel
import ru.wb.perevozka.utils.LogUtils

class CourierLoaderViewModel(
    compositeDisposable: CompositeDisposable,
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val tokenManager: TokenManager,
    private val courierLocalRepository: CourierLocalRepository,
    private val appRemoteRepository: AppRemoteRepository,
    private val authRemoteRepository: AuthRemoteRepository,
) : NetworkViewModel(compositeDisposable) {

    private val _drawerHeader = MutableLiveData<UserInfoEntity>()
    val drawerHeader: LiveData<UserInfoEntity>
        get() = _drawerHeader

    private val _navigationDrawerState = MutableLiveData<CourierLoaderNavigationState>()
    val navigationDrawerState: LiveData<CourierLoaderNavigationState>
        get() = _navigationDrawerState

    init {
        initDrawer()
        // TODO: 24.09.2021 выключить для тестирования
        //toCourierWarehouse()
        //toLoadingScanner()
        //toIntransit()
        checkUserState()
    }

    private fun initDrawer() {
        _drawerHeader.value =  UserInfoEntity(tokenManager.userName(), "")
    }

    private fun checkUserState() {
        val phone = tokenManager.userPhone()
        when {
            tokenManager.resources().contains(NEED_SEND_COURIER_DOCUMENTS) -> toUserForm(phone)
            tokenManager.resources().contains(NEED_APPROVE_COURIER_DOCUMENTS) ->
                toCouriersCompleteRegistration(phone)
            else -> {
                addSubscription(
                    appRemoteRepository.tasksMy()
                        .compose(rxSchedulerFactory.applySingleSchedulers())
                        .subscribe({ tasksMyComplete(it) }, { tasksMyError(it) })
                )

            }
        }
    }

    private fun tasksMyComplete(courierTasksMyEntity: CourierTasksMyEntity) {
        saveCurrentOrderAndOffices(courierTasksMyEntity)
        switchNavigationState(courierTasksMyEntity)
    }

    private fun switchNavigationState(courierTasksMyEntity: CourierTasksMyEntity) {
        if (courierTasksMyEntity.status.isEmpty()) {
            toTimer()
        } else if (courierTasksMyEntity.status == "started") {
            toLoadingScanner()
        } else if (courierTasksMyEntity.status == "intransit") {
            toIntransit()
        }
    }

    private fun saveCurrentOrderAndOffices(courierTasksMyEntity: CourierTasksMyEntity) {
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

        val courierOrderDstOfficesLocalEntity = mutableListOf<CourierOrderDstOfficeLocalEntity>()
        courierTasksMyEntity.dstOffices.forEach {
            with(it) {
                courierOrderDstOfficesLocalEntity.add(
                    CourierOrderDstOfficeLocalEntity(
                        id = id,
                        orderId = courierOrderLocalEntity.id,
                        name = name,
                        fullAddress = fullAddress,
                        longitude = long,
                        latitude = lat,
                        visitedAt = ""
                    )
                )
            }
        }

        courierLocalRepository.deleteAllOrder()
        courierLocalRepository.deleteAllOrderOffices()
        val saveCurrentOrderAndOffices = courierLocalRepository.saveCurrentOrderAndOffices(
            courierOrderLocalEntity,
            courierOrderDstOfficesLocalEntity
        ).compose(rxSchedulerFactory.applyCompletableSchedulers())
        addSubscription(saveCurrentOrderAndOffices.subscribe({}, {}))
    }

    private fun tasksMyError(throwable: Throwable) {
        when (throwable) {
            is NullPointerException -> toCourierWarehouse()
            is NoInternetException -> {
            }
            is BadRequestException -> {
            }
            else -> {
            }
        }
    }

    private fun toUserForm(phone: String) {
        _navigationDrawerState.value = CourierLoaderNavigationState.NavigateToCourierUserForm(phone)
    }

    private fun toCouriersCompleteRegistration(phone: String) {
        _navigationDrawerState.value =
            CourierLoaderNavigationState.NavigateToCouriersCompleteRegistration(phone)
    }

    private fun toCourierWarehouse() {
        _navigationDrawerState.value = CourierLoaderNavigationState.NavigateToCourierWarehouse
    }

    private fun toTimer() {
        _navigationDrawerState.value = CourierLoaderNavigationState.NavigateToTimer
    }

    private fun toLoadingScanner() {
        _navigationDrawerState.value = CourierLoaderNavigationState.NavigateToScanner
    }

    private fun toIntransit() {
        _navigationDrawerState.value = CourierLoaderNavigationState.NavigateToIntransit
    }

}