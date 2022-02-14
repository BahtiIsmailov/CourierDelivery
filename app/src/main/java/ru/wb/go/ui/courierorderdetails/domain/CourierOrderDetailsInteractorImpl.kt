package ru.wb.go.ui.courierorderdetails.domain

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import ru.wb.go.db.CourierLocalRepository
import ru.wb.go.db.entity.TaskStatus
import ru.wb.go.db.entity.courierlocal.CourierOrderLocalDataEntity
import ru.wb.go.db.entity.courierlocal.LocalOrderEntity
import ru.wb.go.network.api.app.AppRemoteRepository
import ru.wb.go.network.rx.RxSchedulerFactory
import ru.wb.go.network.token.TokenManager
import ru.wb.go.network.token.UserManager
import ru.wb.go.ui.couriermap.CourierMapAction
import ru.wb.go.ui.couriermap.CourierMapState
import ru.wb.go.ui.couriermap.domain.CourierMapRepository
import ru.wb.go.utils.managers.TimeManager

class CourierOrderDetailsInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val appRemoteRepository: AppRemoteRepository,
    private val locRepo: CourierLocalRepository,
    private val userManager: UserManager,
    private val courierMapRepository: CourierMapRepository,
    private val timeManager: TimeManager,
    private val tokenManager: TokenManager,
) : CourierOrderDetailsInteractor {

    override fun observeOrderData(): Flowable<CourierOrderLocalDataEntity> {
        return locRepo.observeOrderData()
            .compose(rxSchedulerFactory.applyFlowableSchedulers())
    }

    override fun carNumberIsConfirm(): Boolean {
        return userManager.carNumber().isNotEmpty()
    }

    override fun carNumber(): String {
        return userManager.carNumber()
    }

    override fun observeMapAction(): Observable<CourierMapAction> {
        return courierMapRepository.observeMapAction()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    override fun mapState(state: CourierMapState) {
        courierMapRepository.mapState(state)
    }

    override fun anchorTask(): Completable {

        val reservedTime = timeManager.getLocalTime()

        val order = locRepo.orderData()!!

        return appRemoteRepository.reserveTask(
            order.courierOrderLocalEntity.id.toString(),
            userManager.carNumber()
        )
            .doOnComplete {
                val wh = locRepo.readCurrentWarehouse().blockingGet()
                val ro =
                    with(order.courierOrderLocalEntity) {
                        LocalOrderEntity(
                            orderId = id,
                            routeID = routeID,
                            gate = gate,
                            minPrice = minPrice,
                            minVolume = minVolume,
                            minBoxes = minBoxesCount,
                            countOffices = order.dstOffices.size,
                            wbUserID = -1,
                            carNumber = userManager.carNumber(),
                            reservedAt = reservedTime,
                            startedAt = "",
                            reservedDuration = reservedDuration,
                            status = TaskStatus.TIMER.status,
                            cost = 0,
                            srcId = wh.id,
                            srcName = wh.name,
                            srcAddress = wh.fullAddress,
                            srcLongitude = wh.longitude,
                            srcLatitude = wh.latitude,
                        )
                    }
                locRepo.setOrderInReserve(ro)
            }
            .compose(rxSchedulerFactory.applyCompletableSchedulers())
    }

    override fun isDemoMode(): Boolean {
        return tokenManager.isDemo()
    }

}