package ru.wb.go.ui.courierintransit.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.wb.go.db.CourierLocalRepository
import ru.wb.go.db.IntransitTimeRepository
import ru.wb.go.db.entity.courierlocal.LocalBoxEntity
import ru.wb.go.db.entity.courierlocal.LocalOfficeEntity
import ru.wb.go.db.entity.courierlocal.LocalOrderEntity
import ru.wb.go.network.api.app.AppRemoteRepository
import ru.wb.go.network.monitor.NetworkMonitorRepository
import ru.wb.go.ui.BaseServiceInteractorImpl
import ru.wb.go.ui.couriermap.CourierMapAction
import ru.wb.go.ui.couriermap.CourierMapState
import ru.wb.go.ui.couriermap.domain.CourierMapRepository
import ru.wb.go.utils.managers.DeviceManager
import ru.wb.go.utils.managers.TimeManager

class CourierIntransitInteractorImpl(
    networkMonitorRepository: NetworkMonitorRepository,
    deviceManager: DeviceManager,
    private val remoteRepo: AppRemoteRepository,
    private val locRepo: CourierLocalRepository,
    private val intransitTimeRepository: IntransitTimeRepository,
    private val timeManager: TimeManager,
    private val courierMapRepository: CourierMapRepository,
) : BaseServiceInteractorImpl(networkMonitorRepository, deviceManager),
    CourierIntransitInteractor {


    override fun getOffices(): Flow<List<LocalOfficeEntity>> {
        return locRepo.getOfficesFlowable()
            .map {
                it.toMutableList().sortedWith(
                    compareBy({ it.isVisited }, { it.deliveredBoxes == it.countBoxes }))
            }

    }

    override fun observeOrderTimer(): Flow<Long> {
        return intransitTimeRepository.startTimer()
            .map {
                val order = locRepo.getOrder()
                val offsetSec = timeManager.getPassedTime(order?.startedAt?:"")
                it + offsetSec
            }
    }



    override suspend fun setIntransitTask(orderId: String, boxes: List<LocalBoxEntity>) {
        remoteRepo.setIntransitTask(orderId, boxes)
        locRepo.setOnlineOffices()
    }

    override suspend fun completeDelivery(order: LocalOrderEntity) {
        remoteRepo.taskStatusesEnd(order.orderId.toString())
    }

    override suspend fun clearLocalTaskData() {
        timeManager.clear()
        locRepo.clearOrder()
    }

    override suspend fun getOrder(): LocalOrderEntity? {
        return locRepo.getOrder()
    }

    override suspend fun getOrderId(): String {
        return getOrder()?.orderId.toString()
    }

    override fun observeMapAction(): Flow<CourierMapAction> {
        return courierMapRepository.observeMapAction()
    }

    override  fun mapState(state: CourierMapState) {
        return courierMapRepository.mapState(state)

    }

    override suspend fun getOfflineBoxes(): List<LocalBoxEntity> {
          return locRepo.getOfflineBoxes()

    }

    override suspend fun getBoxes(): List<LocalBoxEntity> {
        return locRepo.getBoxes()
    }
}

data class CompleteDeliveryResult(val deliveredBoxes: Int, val countBoxes: Int, val cost: Int)

