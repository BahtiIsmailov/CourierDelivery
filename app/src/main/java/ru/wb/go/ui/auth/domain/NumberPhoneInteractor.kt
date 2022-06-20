package ru.wb.go.ui.auth.domain

import io.reactivex.Completable
import ru.wb.go.network.monitor.NetworkState
import io.reactivex.Observable
import kotlinx.coroutines.flow.Flow

interface NumberPhoneInteractor {

    fun userPhone() : String

    suspend fun couriersExistAndSavePhone(phone: String)

    fun observeNetworkConnected():  Flow<NetworkState>
}