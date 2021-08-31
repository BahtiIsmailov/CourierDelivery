package ru.wb.perevozka.ui.courierorderdetails.domain

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import ru.wb.perevozka.app.DELAY_NETWORK_REQUEST_MS
import ru.wb.perevozka.db.CourierLocalRepository
import ru.wb.perevozka.db.entity.courierlocal.CourierOrderLocalDataEntity
import ru.wb.perevozka.network.api.app.AppRemoteRepository
import ru.wb.perevozka.network.api.app.entity.CourierAnchorEntity
import ru.wb.perevozka.network.rx.RxSchedulerFactory
import java.util.concurrent.TimeUnit

class CourierOrderDetailsInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val appRemoteRepository: AppRemoteRepository,
    private val courierLocalRepository: CourierLocalRepository
) : CourierOrderDetailsInteractor {

    @Deprecated("Перенести далее по flow")
    override fun anchorTask(taskID: String): Single<CourierAnchorEntity> {
        return Completable.timer(DELAY_NETWORK_REQUEST_MS, TimeUnit.MILLISECONDS)
            .andThen(appRemoteRepository.anchorTask(taskID))
            .compose(rxSchedulerFactory.applySingleSchedulers())
        // TODO: 24.08.2021 выключено для тестирования
//        return appRemoteRepository.anchorTask(taskID)
//            .compose(rxSchedulerFactory.applySingleSchedulers())
    }

    override fun observeOrderData(): Flowable<CourierOrderLocalDataEntity> {
        return courierLocalRepository.observeOrderData()
            .compose(rxSchedulerFactory.applyFlowableSchedulers())
    }

}