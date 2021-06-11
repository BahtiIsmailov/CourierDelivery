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

    override fun observeFlight(): Flowable<Optional<FlightData>> {
        return appLocalRepository.observeFlightData()
            .compose(rxSchedulerFactory.applyFlowableSchedulers())
    }

    override fun observeFlightBoxScanned(): Flowable<Int> {
        return appLocalRepository.observeAttachedBoxes().map { it.size }
            .compose(rxSchedulerFactory.applyFlowableSchedulers())
    }

    override fun deleteFlightBoxes(): Completable {
        return appLocalRepository.observeAttachedBoxes()
            .toObservable()
            .flatMapIterable { it }
            .flatMapCompletable {
                deleteScannedFlightBoxRemote(it).andThen(deleteScannedFlightBoxLocal(it))
            }
            .compose(rxSchedulerFactory.applyCompletableSchedulers())
    }

    private fun deleteScannedFlightBoxRemote(attachedBoxEntity: AttachedBoxEntity) =
        with(attachedBoxEntity) {
            appRemoteRepository.removeBoxFromFlight(
                flightId.toString(),
                barcode,
                isManualInput,
                updatedAt,
                srcOffice.id)
        }

    private fun deleteScannedFlightBoxLocal(attachedBoxEntity: AttachedBoxEntity) =
        appLocalRepository.deleteAttachedBox(attachedBoxEntity).onErrorComplete()

}