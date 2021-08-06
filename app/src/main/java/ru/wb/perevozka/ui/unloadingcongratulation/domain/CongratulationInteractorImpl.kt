package ru.wb.perevozka.ui.unloadingcongratulation.domain

import ru.wb.perevozka.db.AppLocalRepository
import ru.wb.perevozka.network.rx.RxSchedulerFactory
import io.reactivex.Single

class CongratulationInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val appLocalRepository: AppLocalRepository,
) : CongratulationInteractor {

    override fun getDeliveryBoxesGroupByOffice(): Single<DeliveryResult> {
        return appLocalRepository.getCongratulationDelivered()
            .compose(rxSchedulerFactory.applySingleSchedulers())
    }

}

data class DeliveryResult(val unloadedCount: Int, val attachedCount: Int)
