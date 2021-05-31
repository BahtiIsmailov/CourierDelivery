package com.wb.logistics.utils.managers

import androidx.navigation.NavDirections
import com.wb.logistics.app.AppPreffsKeys
import com.wb.logistics.db.AppLocalRepository
import com.wb.logistics.db.SuccessOrEmptyData
import com.wb.logistics.network.api.app.AppRemoteRepository
import com.wb.logistics.network.api.app.FlightStatus
import com.wb.logistics.network.api.app.remote.flightsstatus.StatusLocationEntity
import com.wb.logistics.network.api.app.remote.flightsstatus.StatusOfficeLocationEntity
import com.wb.logistics.network.api.app.remote.flightsstatus.StatusStateEntity
import com.wb.logistics.network.rx.RxSchedulerFactory
import com.wb.logistics.network.token.TimeManager
import com.wb.logistics.ui.flightloader.FlightLoaderFragmentDirections
import com.wb.logistics.ui.unloading.UnloadingScanParameters
import com.wb.logistics.utils.prefs.SharedWorker
import io.reactivex.Completable
import io.reactivex.Single

class ScreenManagerImpl(
    private val worker: SharedWorker,
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val appRemoteRepository: AppRemoteRepository,
    private val appLocalRepository: AppLocalRepository,
    private val timeManager: TimeManager,
) : ScreenManager {

    override fun loadState(): Single<NavDirections> {
        val flightId = appRemoteRepository.flight().map { it.id.toString() }
        return flightId.flatMap { appRemoteRepository.getFlightStatus(it) }
            .doOnSuccess { worker.save(AppPreffsKeys.SCREEN_KEY, it) }
            .map {
                navDirectionsByStatus(FlightStatus.valueOf(it.status.toUpperCase()),
                    it.location.office.id)
            }
            .onErrorReturn { navDirectionsByStatus(FlightStatus.CLOSED, 0) }
            .compose(rxSchedulerFactory.applySingleSchedulers())
    }

    private fun navDirectionsByStatus(status: FlightStatus, dstOfficeId: Int) =
        with(FlightLoaderFragmentDirections) {
            when (status) {
                FlightStatus.ASSIGNED -> actionFlightLoaderFragmentToFlightsFragment()
                FlightStatus.DCLOADING -> actionFlightLoaderFragmentToDcLoadingFragment()
                FlightStatus.INTRANSIT -> actionFlightLoaderFragmentToFlightDeliveriesFragment()
                FlightStatus.UNLOADING -> actionFlightLoaderFragmentToUnloadingScanFragment(
                    UnloadingScanParameters(dstOfficeId))
                FlightStatus.DCUNLOADING -> actionFlightLoaderFragmentToCongratulationFragment()
                FlightStatus.CLOSED -> actionFlightLoaderFragmentToFlightsFragment()
            }
        }

    override fun saveState(flightStatus: FlightStatus, isGetFromGPS: Boolean): Completable {
        return appLocalRepository.readFlight()
            .flatMapCompletable {
                if (it is SuccessOrEmptyData.Success)
                    with(it.data) {
                        val flightId = id.toString()
                        val officeId = dc.id
                        val updatedAt = timeManager.getOffsetLocalTime()
                        saveState(flightId, flightStatus, officeId, updatedAt, isGetFromGPS)
                    }
                else Completable.error(Throwable())
            }
    }

    private fun saveState(
        flightId: String,
        flightStatus: FlightStatus,
        officeId: Int,
        updatedAt: String,
        isGetFromGPS: Boolean,
    ): Completable {
        val statusStateEntity = loadStatusStateEntity()
        if (statusStateEntity != null && flightStatus == FlightStatus.valueOf(statusStateEntity.status.toUpperCase())) return Completable.complete()
        val putStatus =
            appRemoteRepository.putFlightStatus(flightId,
                flightStatus,
                officeId,
                isGetFromGPS,
                updatedAt)
        val convertStatus = Single.just(StatusStateEntity(flightStatus.status, StatusLocationEntity(
            StatusOfficeLocationEntity(officeId), isGetFromGPS)))
        return Completable.fromSingle(putStatus.andThen(convertStatus)
            .doOnSuccess { worker.save(AppPreffsKeys.SCREEN_KEY, it) })
            .compose(rxSchedulerFactory.applyCompletableSchedulers())
    }

    override fun saveState(
        flightStatus: FlightStatus,
        officeId: Int,
        isGetFromGPS: Boolean,
    ): Completable {
        return appLocalRepository.readFlight()
            .flatMapCompletable {
                if (it is SuccessOrEmptyData.Success)
                    with(it.data) {
                        val flightId = id.toString()
                        val updatedAt = timeManager.getOffsetLocalTime()
                        saveState(flightId, flightStatus, officeId, updatedAt, isGetFromGPS)
                    }
                else Completable.error(Throwable())
            }
    }

    override fun readState(): NavDirections {
        val statusStateEntity = loadStatusStateEntity()
        return if (statusStateEntity == null) FlightLoaderFragmentDirections.actionFlightLoaderFragmentToFlightsFragment()
        else navDirectionsByStatus(FlightStatus.valueOf(statusStateEntity.status),
            statusStateEntity.location.office.id)
    }

    private fun loadStatusStateEntity() =
        worker.load(AppPreffsKeys.SCREEN_KEY, StatusStateEntity::class.java)

    override fun clear() {
        worker.delete(AppPreffsKeys.SCREEN_KEY)
    }

}