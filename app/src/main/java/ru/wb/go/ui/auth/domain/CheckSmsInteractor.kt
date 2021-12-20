package ru.wb.go.ui.auth.domain

import com.jakewharton.rxbinding3.InitialValueObservable
import ru.wb.go.network.monitor.NetworkState
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import ru.wb.go.network.api.auth.entity.TokenEntity
import ru.wb.go.ui.auth.signup.TimerState
import ru.wb.go.ui.auth.signup.TimerState

interface CheckSmsInteractor {
    fun remindPasswordChanges(observable: InitialValueObservable<CharSequence>): Observable<Boolean>
    fun observeNetworkConnected(): Observable<NetworkState>
    fun auth(phone: String, password: String): Completable
    fun couriersExistAndSavePhone(phone: String) : Completable
    fun startTimer(durationTime: Int)
    val timer: Flowable<TimerState>
    fun stopTimer()
}