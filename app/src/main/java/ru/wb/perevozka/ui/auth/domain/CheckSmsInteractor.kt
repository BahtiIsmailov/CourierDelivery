package ru.wb.perevozka.ui.auth.domain

import com.jakewharton.rxbinding3.InitialValueObservable
import ru.wb.perevozka.network.monitor.NetworkState
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import ru.wb.perevozka.network.api.auth.entity.TokenEntity
import ru.wb.perevozka.ui.auth.signup.TimerState

interface CheckSmsInteractor {
    fun remindPasswordChanges(observable: InitialValueObservable<CharSequence>): Observable<Boolean>
    fun observeNetworkConnected(): Observable<NetworkState>
    fun auth(phone: String, password: String): Single<CheckSmsData>
    fun couriersExistAndSavePhone(phone: String) : Completable
    fun startTimer(durationTime: Int)
    val timer: Flowable<TimerState>
    fun stopTimer()
}