package ru.wb.perevozka.ui.courierorderdetails.domain

import io.reactivex.Completable
import io.reactivex.Single
import ru.wb.perevozka.network.api.app.AppRemoteRepository
import ru.wb.perevozka.network.api.app.entity.CourierAnchorEntity
import ru.wb.perevozka.network.rx.RxSchedulerFactory
import java.util.concurrent.TimeUnit

class CourierOrderDetailsInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val appRemoteRepository: AppRemoteRepository,
) : CourierOrderDetailsInteractor {

    override fun anchorTask(taskID: String): Single<CourierAnchorEntity> {
        return Completable.timer(1000, TimeUnit.MILLISECONDS)
            .andThen(appRemoteRepository.anchorTask(taskID))
            .compose(rxSchedulerFactory.applySingleSchedulers())
        // TODO: 24.08.2021 выключено для тестирования
//        return appRemoteRepository.anchorTask(taskID)
//            .compose(rxSchedulerFactory.applySingleSchedulers())
    }

}