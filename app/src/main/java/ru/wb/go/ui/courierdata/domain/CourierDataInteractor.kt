package ru.wb.go.ui.courierdata.domain

import kotlinx.coroutines.flow.Flow
import ru.wb.go.network.api.app.entity.CourierDocumentsEntity
import ru.wb.go.network.monitor.NetworkState

interface CourierDataInteractor {

    fun observeNetworkConnected(): Flow<NetworkState>

    suspend fun saveCourierDocuments(courierDocumentsEntity: CourierDocumentsEntity)

    suspend fun getCourierDocuments():  CourierDocumentsEntity
}