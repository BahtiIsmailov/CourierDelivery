package ru.wb.go.ui.courierwarehouses.domain

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import ru.wb.go.app.DELAY_NETWORK_REQUEST_MS
import ru.wb.go.db.CourierLocalRepository
import ru.wb.go.db.entity.courier.CourierWarehouseLocalEntity
import ru.wb.go.network.api.app.AppTasksRepository
import ru.wb.go.network.monitor.NetworkMonitorRepository
import ru.wb.go.network.rx.RxSchedulerFactory
import ru.wb.go.network.token.TokenManager
import ru.wb.go.ui.BaseServiceInteractorImpl
import ru.wb.go.ui.couriermap.CourierMapAction
import ru.wb.go.ui.couriermap.CourierMapState
import ru.wb.go.ui.couriermap.domain.CourierMapRepository
import ru.wb.go.utils.CoroutineExtension
import ru.wb.go.utils.managers.DeviceManager
import java.util.concurrent.TimeUnit

class CourierWarehousesInteractorImpl(
    rxSchedulerFactory: RxSchedulerFactory,
    networkMonitorRepository: NetworkMonitorRepository,
    deviceManager: DeviceManager,
    private val appRemoteRepository: AppTasksRepository,
    private val courierLocalRepository: CourierLocalRepository,
    private val courierMapRepository: CourierMapRepository,
    private val tokenManager: TokenManager
) : BaseServiceInteractorImpl(rxSchedulerFactory, networkMonitorRepository, deviceManager),
    CourierWarehousesInteractor {

    override suspend fun getWarehouses(): List<CourierWarehouseLocalEntity> {
        return appRemoteRepository.courierWarehouses()

    }
//    override suspend fun getWarehouses(): Single<List<CourierWarehouseLocalEntity>> {
//        return appRemoteRepository.courierWarehouses()
//            .compose(rxSchedulerFactory.applySingleSchedulers())
//    }

    override suspend fun clearAndSaveCurrentWarehouses(courierWarehouseEntity: CourierWarehouseLocalEntity) {
        courierLocalRepository.deleteAllWarehouse()
        courierLocalRepository.saveCurrentWarehouse(courierWarehouseEntity)
    }

    override fun loadProgress() {
        CoroutineExtension.interval(DELAY_NETWORK_REQUEST_MS, TimeUnit.MILLISECONDS)
    }

    override fun observeMapAction(): Flow<CourierMapAction> {
        return courierMapRepository.observeMapAction()
    }

    override fun mapState(state: CourierMapState) {
        courierMapRepository.mapState(state)
    }


    override fun mapAction(action: CourierMapAction) {
        courierMapRepository.mapAction(action)
    }

    override fun isDemoMode(): Boolean {
        return tokenManager.isDemo()
    }
}
