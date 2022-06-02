package ru.wb.go.ui.courierdata.domain

import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.coroutines.Dispatchers
import ru.wb.go.network.api.app.AppRemoteRepository
import ru.wb.go.network.api.app.entity.CourierDocumentsEntity
import ru.wb.go.network.api.app.toCourierDocumentsEntity
import ru.wb.go.network.exceptions.InternalServerException
import ru.wb.go.network.monitor.NetworkMonitorRepository
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.network.rx.RxSchedulerFactory
import ru.wb.go.network.token.UserManager
import java.lang.Exception

class CourierDataInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val networkMonitorRepository: NetworkMonitorRepository,
    private val appRemoteRepository: AppRemoteRepository,
    private val userManager: UserManager,
) : CourierDataInteractor {

    override fun observeNetworkConnected(): Observable<NetworkState> {
        return networkMonitorRepository.networkConnected()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    override suspend fun saveCourierDocuments(courierDocumentsEntity: CourierDocumentsEntity)  {
        return with(Dispatchers.IO){
            try{
                appRemoteRepository.saveCourierDocuments(courierDocumentsEntity)
            }catch (e:Exception){
                if (e is InternalServerException){
                    userManager.saveCourierDocumentsEntity(courierDocumentsEntity)
                }
            }
        }
    }

    override suspend fun getCourierDocuments(): CourierDocumentsEntity {
        return with(Dispatchers.IO) {
            appRemoteRepository.getCourierDocuments()
        }
    }
}