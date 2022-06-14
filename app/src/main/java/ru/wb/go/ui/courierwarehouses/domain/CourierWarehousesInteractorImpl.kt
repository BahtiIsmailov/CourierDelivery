package ru.wb.go.ui.courierwarehouses.domain

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
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

    override suspend fun clearAndSaveCurrentWarehouses(courierWarehouseEntity: CourierWarehouseLocalEntity)  {
        courierLocalRepository.deleteAllWarehouse()
        return withContext(Dispatchers.IO){
            courierLocalRepository.saveCurrentWarehouse(courierWarehouseEntity)
        }
    }

    override suspend fun loadProgress()  {
        return withContext(Dispatchers.IO){
            Completable.timer(DELAY_NETWORK_REQUEST_MS, TimeUnit.MILLISECONDS)
        }
    }

    override suspend fun observeMapAction(): Flow<CourierMapAction> {
        return  withContext(Dispatchers.IO){
            courierMapRepository.observeMapAction()
         }
    }

    override suspend fun mapState(state: CourierMapState) {
        courierMapRepository.mapState(state)
    }

    override fun isDemoMode(): Boolean {
        return tokenManager.isDemo()
    }

}