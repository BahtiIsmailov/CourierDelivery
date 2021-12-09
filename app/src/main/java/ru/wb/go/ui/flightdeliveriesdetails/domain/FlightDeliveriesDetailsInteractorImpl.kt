package ru.wb.go.ui.flightdeliveriesdetails.domain

import ru.wb.go.db.AppLocalRepository
import ru.wb.go.network.rx.RxSchedulerFactory
import io.reactivex.Single

class FlightDeliveriesDetailsInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val appLocalRepository: AppLocalRepository,
) : FlightDeliveriesDetailsInteractor {

    override fun getUnloadedAndReturnBoxesGroupByOffice(currentOfficeId: Int): Single<UnloadedAndReturnBoxesGroupByOffice> {
        return Single.zip(
            appLocalRepository.observeDeliveryUnloadedFlightBoxesByOfficeId(currentOfficeId)
                .firstOrError(),
            appLocalRepository.observeReturnedFlightBoxesByOfficeId(currentOfficeId).firstOrError(),
            { unloadedBoxes, returnBoxes ->
                UnloadedAndReturnBoxesGroupByOffice(unloadedBoxes, returnBoxes)
            }
        ).compose(rxSchedulerFactory.applySingleSchedulers())
    }

}