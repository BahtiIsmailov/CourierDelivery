package ru.wb.go.ui.courierwarehouses.domain

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import ru.wb.go.app.DELAY_NETWORK_REQUEST_MS
import ru.wb.go.db.CourierLocalRepository
import ru.wb.go.db.entity.courier.CourierWarehouseLocalEntity
import ru.wb.go.network.api.app.AppRemoteRepository
import ru.wb.go.network.rx.RxSchedulerFactory
import ru.wb.go.network.token.UserManager
import ru.wb.go.ui.app.domain.AppSharedRepository
import ru.wb.go.ui.couriermap.CourierMapAction
import ru.wb.go.ui.couriermap.CourierMapState
import ru.wb.go.ui.couriermap.domain.CourierMapRepository
import java.util.concurrent.TimeUnit

class CourierWarehouseInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val appRemoteRepository: AppRemoteRepository,
    private val appSharedRepository: AppSharedRepository,
    private val courierLocalRepository: CourierLocalRepository,
    private val courierMapRepository: CourierMapRepository,
    private val userManager: UserManager
) : CourierWarehouseInteractor {

    override fun getServerWarehouses(): Single<List<CourierWarehouseLocalEntity>> {
        return appRemoteRepository.courierWarehouses()
            .compose(rxSchedulerFactory.applySingleSchedulers())
    }

    override fun clearAndSaveCurrentWarehouses(courierWarehouseEntity: CourierWarehouseLocalEntity): Completable {
        courierLocalRepository.deleteAllWarehouse()
        return courierLocalRepository.saveCurrentWarehouse(courierWarehouseEntity)
            .compose(rxSchedulerFactory.applyCompletableSchedulers())
    }


    override fun observeSearch(): Observable<String> {
        return appSharedRepository.observeSearch()
            .debounce(DELAY_NETWORK_REQUEST_MS, TimeUnit.MILLISECONDS)
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    override fun loadProgress(): Completable {
        return Completable.timer(DELAY_NETWORK_REQUEST_MS, TimeUnit.MILLISECONDS)
            .compose(rxSchedulerFactory.applyCompletableSchedulers())
    }

    override fun observeMapAction(): Observable<CourierMapAction> {
        return courierMapRepository.observeMapAction()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    override fun mapState(state: CourierMapState) {
        courierMapRepository.mapState(state)
    }

    override fun isDemoMode(): Boolean {
        return userManager.isDemoMode()
    }

}