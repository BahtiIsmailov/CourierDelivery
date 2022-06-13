package ru.wb.go.ui.courierintransit.domain

import io.reactivex.Observable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.wb.go.db.CourierLocalRepository
import ru.wb.go.db.IntransitTimeRepository
import ru.wb.go.db.entity.courierlocal.LocalBoxEntity
import ru.wb.go.db.entity.courierlocal.LocalOfficeEntity
import ru.wb.go.db.entity.courierlocal.LocalOrderEntity
import ru.wb.go.network.api.app.AppRemoteRepository
import ru.wb.go.network.monitor.NetworkMonitorRepository
import ru.wb.go.network.rx.RxSchedulerFactory
import ru.wb.go.ui.BaseServiceInteractorImpl
import ru.wb.go.ui.couriermap.CourierMapAction
import ru.wb.go.ui.couriermap.CourierMapState
import ru.wb.go.ui.couriermap.domain.CourierMapRepository
import ru.wb.go.utils.managers.DeviceManager
import ru.wb.go.utils.managers.TimeManager

class CourierIntransitInteractorImpl(
    rxSchedulerFactory: RxSchedulerFactory,
    networkMonitorRepository: NetworkMonitorRepository,
    deviceManager: DeviceManager,
    private val remoteRepo: AppRemoteRepository,
    private val locRepo: CourierLocalRepository,
    private val intransitTimeRepository: IntransitTimeRepository,
    private val timeManager: TimeManager,
    private val courierMapRepository: CourierMapRepository,
) : BaseServiceInteractorImpl(rxSchedulerFactory, networkMonitorRepository, deviceManager),
    CourierIntransitInteractor {

//    override fun getOffices(): Observable<List<LocalOfficeEntity>> {
//        return locRepo.getOfficesFlowable()
//            .toObservable()
//            .map { office ->
//                office.toMutableList().sortedWith(
//                    compareBy({ it.isVisited }, { it.deliveredBoxes == it.countBoxes })
//                )
//            }
//            .compose(rxSchedulerFactory.applyObservableSchedulers())
//    }

    override suspend fun getOffices(): List<LocalOfficeEntity> {
        return withContext(Dispatchers.IO) {
            locRepo.getOfficesFlowable().toMutableList().sortedWith(
                compareBy({ it.isVisited }, { it.deliveredBoxes == it.countBoxes })
            )

        }
    }

    override suspend fun observeOrderTimer(): Long {
        return withContext(Dispatchers.IO) {
            val order = locRepo.getOrder()
            val offsetSec = timeManager.getPassedTime(order.startedAt)
            intransitTimeRepository.startTimer()
            offsetSec
        }
    }

//    override fun observeOrderTimer(): Observable<Long> {
//        val order = locRepo.getOrder()!!
//        // TODO: 25.11.2021 переработать с учетом часового пояса
//        val offsetSec = timeManager.getPassedTime(order.startedAt)
//
//        return intransitTimeRepository.startTimer()
//            .toObservable()
//            .map { it + offsetSec }
//            .compose(rxSchedulerFactory.applyObservableSchedulers())
//    }


    override suspend fun setIntransitTask(orderId: String, boxes: List<LocalBoxEntity>) {
        withContext(Dispatchers.IO) {
            remoteRepo.setIntransitTask(orderId, boxes)
            locRepo.setOnlineOffices()
        }
    }

    override suspend fun completeDelivery(order: LocalOrderEntity) {
        return withContext(Dispatchers.IO) {
            remoteRepo.taskStatusesEnd(order.orderId.toString())
        }
    }

    override fun clearLocalTaskData() {
        timeManager.clear()
        locRepo.clearOrder()
    }

    override suspend fun getOrder(): LocalOrderEntity {
        return locRepo.getOrder()
    }

    override suspend fun getOrderId(): String {
        // FIXME: У одного курьера здесь происходит NullPointerException. Причина пока не понятна
        return getOrder().orderId.toString()
    }

    override suspend fun observeMapAction(): CourierMapAction {
        return withContext(Dispatchers.IO){
            courierMapRepository.observeMapAction()
        }
    }

    override fun mapState(state: CourierMapState) {
        courierMapRepository.mapState(state)
    }

    override fun getOfflineBoxes(): List<LocalBoxEntity> {
        return locRepo.getOfflineBoxes()
    }

    override fun getBoxes(): List<LocalBoxEntity> {
        return locRepo.getBoxes()
    }
}

data class CompleteDeliveryResult(val deliveredBoxes: Int, val countBoxes: Int, val cost: Int)