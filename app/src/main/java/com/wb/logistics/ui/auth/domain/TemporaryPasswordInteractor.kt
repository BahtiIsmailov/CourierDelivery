package com.wb.logistics.ui.auth.domain

import com.jakewharton.rxbinding3.InitialValueObservable
import com.wb.logistics.network.api.auth.response.RemainingAttemptsResponse
import com.wb.logistics.ui.auth.signup.TimerState
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single

interface TemporaryPasswordInteractor {
    fun startTimer()
    val timer: Flowable<TimerState>
    fun stopTimer()
    fun clearCountCheckAttempt()
    fun passwordChanges(observable: InitialValueObservable<CharSequence>): Observable<Boolean>
    fun sendTmpPassword(phone: String): Single<RemainingAttemptsResponse>
    fun checkPassword(phone: String, tmpPassword: String): Completable
}