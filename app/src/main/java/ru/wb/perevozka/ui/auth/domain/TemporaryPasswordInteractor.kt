package ru.wb.perevozka.ui.auth.domain

import com.jakewharton.rxbinding3.InitialValueObservable
import ru.wb.perevozka.network.api.auth.response.RemainingAttemptsResponse
import ru.wb.perevozka.network.monitor.NetworkState
import ru.wb.perevozka.ui.auth.signup.TimerState
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single

interface TemporaryPasswordInteractor {
    fun startTimer(durationTime: Int)
    val timer: Flowable<TimerState>
    fun stopTimer()
    fun passwordChanges(observable: InitialValueObservable<CharSequence>): Observable<Boolean>
    fun sendTmpPassword(phone: String): Single<RemainingAttemptsResponse>
    fun checkPassword(phone: String, tmpPassword: String): Completable
    fun observeNetworkConnected(): Observable<NetworkState>
}