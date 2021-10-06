package ru.wb.perevozka.ui.courierorderdetails.domain

import io.reactivex.Flowable
import io.reactivex.Observable
import ru.wb.perevozka.db.CourierLocalRepository
import ru.wb.perevozka.db.entity.courierlocal.CourierOrderLocalDataEntity
import ru.wb.perevozka.network.api.app.AppRemoteRepository
import ru.wb.perevozka.network.rx.RxSchedulerFactory
import ru.wb.perevozka.network.token.UserManager
import ru.wb.perevozka.ui.couriermap.CourierMapAction
import ru.wb.perevozka.ui.couriermap.CourierMapState
import ru.wb.perevozka.ui.couriermap.domain.CourierMapRepository

class CourierOrderDetailsInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val courierLocalRepository: CourierLocalRepository,
    private val userManager: UserManager,
    private val courierMapRepository: CourierMapRepository,
) : CourierOrderDetailsInteractor {

    override fun observeOrderData(): Flowable<CourierOrderLocalDataEntity> {
        return courierLocalRepository.observeOrderData()
            .compose(rxSchedulerFactory.applyFlowableSchedulers())
    }

    override fun carNumberIsConfirm(): Boolean {
        return userManager.carNumber().isNotEmpty()
    }

    override fun observeMapAction(): Observable<CourierMapAction> {
        return courierMapRepository.observeMapAction()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    override fun mapState(state: CourierMapState) {
        courierMapRepository.mapState(state)
    }

}