package ru.wb.go.ui.auth.domain

import kotlinx.coroutines.flow.Flow
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.ui.auth.signup.TimerState

interface CheckSmsInteractor {

    fun remindPasswordChanges(observable: Flow<CharSequence>): Flow<Boolean>
    fun observeNetworkConnected(): Flow<NetworkState>

    suspend fun auth(phone: String, password: String)
    suspend fun couriersExistAndSavePhone(phone: String)

    suspend fun startTimer(durationTime: Int)
    val timer: Flow<TimerState>
    suspend fun stopTimer()
}