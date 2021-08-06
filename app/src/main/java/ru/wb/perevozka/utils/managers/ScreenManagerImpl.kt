package ru.wb.perevozka.utils.managers

import androidx.navigation.NavDirections
import ru.wb.perevozka.app.AppPreffsKeys
import ru.wb.perevozka.db.AppLocalRepository
import ru.wb.perevozka.network.api.app.AppRemoteRepository
import ru.wb.perevozka.network.api.app.FlightStatus
import ru.wb.perevozka.network.api.app.remote.flightsstatus.StatusLocationEntity
import ru.wb.perevozka.network.api.app.remote.flightsstatus.StatusOfficeLocationEntity
import ru.wb.perevozka.network.api.app.remote.flightsstatus.StatusStateEntity
import ru.wb.perevozka.network.rx.RxSchedulerFactory
import ru.wb.perevozka.ui.flightsloader.FlightLoaderFragmentDirections
import ru.wb.perevozka.ui.unloadingscan.UnloadingScanParameters
import ru.wb.perevozka.utils.LogUtils
import ru.wb.perevozka.utils.prefs.SharedWorker
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject

class ScreenManagerImpl(
    private val worker: SharedWorker,
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val appRemoteRepository: AppRemoteRepository,
    private val appLocalRepository: AppLocalRepository,
    private val timeManager: TimeManager,
) : ScreenManager {

    private val navigateSubject = PublishSubject.create<NavigateComplete>()

    data class NavigateComplete(val flightId: String, val flightStatus: FlightStatus)

    override fun observeUpdatedStatus(): Observable<NavigateComplete> {
        return navigateSubject
    }

    override fun navDirection(flightId: String): Single<NavDirections> {
        return appRemoteRepository.getFlightStatus(flightId)
            .doOnSuccess { worker.save(AppPreffsKeys.SCREEN_KEY, it) }
            .map {
                LogUtils { logDebugApp("ScreenManagerImpl navDirection1") }
                val flightStatus = FlightStatus.valueOf(it.status.uppercase())
                val navDir = navDirectionsByStatus(flightStatus, it.location.office.id)
                LogUtils { logDebugApp("ScreenManagerImpl navDirection2 " + flightStatus.toString()) }
                navigateSubject.onNext(NavigateComplete(flightId, flightStatus))
                navDir
            }
            .onErrorReturn {
                LogUtils { logDebugApp("Error " + it.toString()) }
                val flightStatus = FlightStatus.CLOSED
                val navDir = navDirectionsByStatus(flightStatus, 0)
                navigateSubject.onNext(NavigateComplete(flightId, flightStatus))
                navDir
            }
            .compose(rxSchedulerFactory.applySingleSchedulers())
    }

    private fun navDirectionsByStatus(status: FlightStatus, dstOfficeId: Int) =
        with(FlightLoaderFragmentDirections) {
            when (status) {
                FlightStatus.ASSIGNED -> actionFlightLoaderFragmentToFlightsFragment()
                FlightStatus.DCLOADING -> actionFlightLoaderFragmentToFlightsFragment()
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
                with(it) {
                    val flightId = id.toString()
                    val officeId = dc.id
                    val updatedAt = timeManager.getOffsetLocalTime()
                    saveState(flightId, flightStatus, officeId, updatedAt, isGetFromGPS)
                }
            }
        //return Completable.complete()
    }

    private fun saveState(
        flightId: String,
        flightStatus: FlightStatus,
        officeId: Int,
        updatedAt: String,
        isGetFromGPS: Boolean,
    ): Completable {
        val statusStateEntity = loadStatusStateEntity()
        if (statusStateEntity != null
            && flightStatus == FlightStatus.valueOf(statusStateEntity.status.uppercase())
        ) return Completable.complete()
        val putStatus =
            appRemoteRepository.putFlightStatus(flightId,
                flightStatus,
                officeId,
                isGetFromGPS,
                updatedAt)
        val convertStatus = Single.just(StatusStateEntity(flightStatus.status, StatusLocationEntity(
            StatusOfficeLocationEntity(officeId), isGetFromGPS)))
        return Completable.fromSingle(putStatus.andThen(convertStatus)
            .doOnSuccess {
                navigateSubject.onNext(NavigateComplete(flightId, flightStatus))
                worker.save(AppPreffsKeys.SCREEN_KEY, it)
            })
            .compose(rxSchedulerFactory.applyCompletableSchedulers())
    }

    override fun saveState(
        flightStatus: FlightStatus,
        officeId: Int,
        isGetFromGPS: Boolean,
    ): Completable {
        val updatedAt = timeManager.getOffsetLocalTime()
        val updatedVisitedOffice = if (flightStatus == FlightStatus.UNLOADING)
            appLocalRepository.updateFlightOfficeVisited(updatedAt, officeId)
        else Completable.complete()
        val saveState = appLocalRepository.readFlightId()
            .flatMapCompletable { saveState(it, flightStatus, officeId, updatedAt, isGetFromGPS) }
        return saveState.andThen(updatedVisitedOffice)
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