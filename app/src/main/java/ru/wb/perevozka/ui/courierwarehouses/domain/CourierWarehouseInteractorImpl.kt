package ru.wb.perevozka.ui.courierwarehouses.domain

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
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
        return appRemoteRepository.courierWarehouses().delay(400, TimeUnit.MILLISECONDS)
            .compose(rxSchedulerFactory.applySingleSchedulers())
    }

    override fun observeSearch(): Observable<String> {
        return appSharedRepository.observeSearch()
            .debounce(100, TimeUnit.MILLISECONDS)
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    override fun loadProgress(): Completable {
        return Completable.timer(3, TimeUnit.SECONDS)
            .compose(rxSchedulerFactory.applyCompletableSchedulers())
    }

}