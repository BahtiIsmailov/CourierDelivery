package ru.wb.perevozka.ui.courierwarehouses.domain

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import ru.wb.perevozka.app.DELAY_NETWORK_REQUEST_MS
import ru.wb.perevozka.db.entity.courier.CourierWarehouseEntity
import ru.wb.perevozka.network.api.app.AppRemoteRepository
import ru.wb.perevozka.network.rx.RxSchedulerFactory
import ru.wb.perevozka.ui.splash.domain.AppSharedRepository
import java.util.concurrent.TimeUnit

class CourierWarehouseInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val appRemoteRepository: AppRemoteRepository,
    private val appSharedRepository: AppSharedRepository,
) : CourierWarehouseInteractor {

    override fun warehouses(): Single<List<CourierWarehouseEntity>> {
        return appRemoteRepository.courierWarehouses().delay(DELAY_NETWORK_REQUEST_MS, TimeUnit.MILLISECONDS)
            .compose(rxSchedulerFactory.applySingleSchedulers())
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

}