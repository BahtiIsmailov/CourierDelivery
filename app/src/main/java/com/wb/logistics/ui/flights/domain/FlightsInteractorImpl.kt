package com.wb.logistics.ui.flights.domain

import com.wb.logistics.db.AppLocalRepository
import com.wb.logistics.db.FlightData
import com.wb.logistics.db.Optional
import com.wb.logistics.db.entity.attachedboxes.AttachedBoxEntity
import com.wb.logistics.network.api.app.AppRemoteRepository
import com.wb.logistics.network.rx.RxSchedulerFactory
import io.reactivex.Completable
import io.reactivex.Flowable

class FlightsInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val appRemoteRepository: AppRemoteRepository,
    private val appLocalRepository: AppLocalRepository,
) : FlightsInteractor {

    override fun observeFlightData(): Flowable<Optional<FlightData>> {
        return appLocalRepository.observeFlightDataOptional()
            .compose(rxSchedulerFactory.applyFlowableSchedulers())
    }

    override fun observeFlightBoxScanned(): Flowable<Int> {
        return appLocalRepository.observeAttachedBoxes().map { it.size }
            .compose(rxSchedulerFactory.applyFlowableSchedulers())
    }

    override fun deleteFlightBoxes(): Completable {
        return appLocalRepository.readFlight().map { it.id.toString() }
            .flatMapCompletable { flightId ->
                appLocalRepository.observeAttachedBoxes()
                    .toObservable()
                    .flatMapIterable { it }
                    .flatMapCompletable {
                        deleteScannedFlightBoxRemote(flightId, it).andThen(
                            deleteScannedFlightBoxLocal(it))
                    }
            }
            .compose(rxSchedulerFactory.applyCompletableSchedulers())
    }

    private fun deleteScannedFlightBoxRemote(
        flightId: String,
        attachedBoxEntity: AttachedBoxEntity,
    ) = with(attachedBoxEntity) {
        appRemoteRepository.removeBoxFromFlight(
            flightId,
            barcode,
            isManualInput,
            updatedAt,
            srcOffice.id)
    }

    private fun deleteScannedFlightBoxLocal(attachedBoxEntity: AttachedBoxEntity) =
        appLocalRepository.deleteAttachedBox(attachedBoxEntity).onErrorComplete()

}