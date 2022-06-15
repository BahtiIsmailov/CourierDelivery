package ru.wb.go.ui.auth.domain

import com.jakewharton.rxbinding3.InitialValueObservable
import ru.wb.go.network.monitor.NetworkState
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import kotlinx.coroutines.flow.Flow
import ru.wb.go.ui.auth.signup.TimerState

interface CheckSmsInteractor {
    //suspend fun remindPasswordChanges(observable: InitialValueObservable<CharSequence>):  Boolean
    suspend fun observeNetworkConnected(): NetworkState

    suspend fun auth(phone: String, password: String)
    suspend fun couriersExistAndSavePhone(phone: String)

    suspend fun startTimer(durationTime: Int)
    val timer: Flow<TimerState>
    //suspend fun stopTimer()
}