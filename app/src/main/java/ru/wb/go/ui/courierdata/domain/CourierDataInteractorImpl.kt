package ru.wb.go.ui.courierdata.domain

import kotlinx.coroutines.flow.Flow
import ru.wb.go.network.api.app.AppRemoteRepository
import ru.wb.go.network.api.app.entity.CourierDocumentsEntity
import ru.wb.go.network.exceptions.InternalServerException
import ru.wb.go.network.monitor.NetworkMonitorRepository
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.network.token.UserManager

class CourierDataInteractorImpl(
    private val networkMonitorRepository: NetworkMonitorRepository,
    private val appRemoteRepository: AppRemoteRepository,
    private val userManager: UserManager,
) : CourierDataInteractor {

    override fun observeNetworkConnected(): Flow<NetworkState> {
        return networkMonitorRepository.networkConnected()

    }

    override suspend fun saveCourierDocuments(courierDocumentsEntity: CourierDocumentsEntity)  {
        try{
            appRemoteRepository.saveCourierDocuments(courierDocumentsEntity)
        }catch (e:Exception){
            if (e is InternalServerException){
                userManager.saveCourierDocumentsEntity(courierDocumentsEntity)
            }
        }
    }

    override suspend fun getCourierDocuments(): CourierDocumentsEntity {
        return appRemoteRepository.getCourierDocuments()

    }
 }