package ru.wb.go.ui.courierorderdetails.domain

import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import ru.wb.go.db.CourierLocalRepository
import ru.wb.go.db.entity.TaskStatus
import ru.wb.go.db.entity.courierlocal.CourierOrderLocalDataEntity
import ru.wb.go.network.api.app.AppRemoteRepository
import ru.wb.go.network.rx.RxSchedulerFactory
import ru.wb.go.network.token.UserManager
import ru.wb.go.ui.couriermap.CourierMapAction
import ru.wb.go.ui.couriermap.CourierMapState
import ru.wb.go.ui.couriermap.domain.CourierMapRepository

class CourierOrderDetailsInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val appRemoteRepository: AppRemoteRepository,
    private val courierLocalRepository: CourierLocalRepository,
    private val userManager: UserManager,
    private val courierMapRepository: CourierMapRepository,
) : CourierOrderDetailsInteractor {

    companion object {
        const val ORDER_IS_NOT_EXIST = -1
    }

    override fun observeOrderData(): Flowable<CourierOrderLocalDataEntity> {
        return courierLocalRepository.observeOrderData()
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

    override fun anchorTask(): Single<AnchorTaskStatus> {
        return courierLocalRepository.readCurrentWarehouse()
            .map { it.id }
            .flatMap { appRemoteRepository.courierOrders(it) }
            .zipWith(
                courierLocalRepository.orderDataSync().map { it.courierOrderLocalEntity },
                { remoteOrders, localOrder ->
                    val localOrderId = localOrder.id
                    val orderExist = remoteOrders.find { it.id == localOrderId }
                    if (orderExist == null) ORDER_IS_NOT_EXIST else localOrderId
                })
            .flatMap {
                when (it) {
                    ORDER_IS_NOT_EXIST -> Single.just(AnchorTaskStatus.TaskIsNotExist)
                    else -> {
                        appRemoteRepository.anchorTask(it.toString(), userManager.carNumber())
                            .andThen(Single.just(AnchorTaskStatus.AnchorTaskComplete))
                    }
                }
            }
            .doOnSuccess { userManager.saveStatusTask(TaskStatus.TIMER.status) }
            .compose(rxSchedulerFactory.applySingleSchedulers())
    }

}

sealed class AnchorTaskStatus {
    object AnchorTaskComplete : AnchorTaskStatus()
    object TaskIsNotExist : AnchorTaskStatus()
}