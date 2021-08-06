package ru.wb.perevozka.ui.auth.domain

import com.jakewharton.rxbinding3.InitialValueObservable
import ru.wb.perevozka.network.monitor.NetworkState
import io.reactivex.Completable
import io.reactivex.Observable

interface InputPasswordInteractor {
    fun remindPasswordChanges(observable: InitialValueObservable<CharSequence>): Observable<Boolean>
    fun authByPassword(phone: String, password: String): Completable
    fun observeNetworkConnected(): Observable<NetworkState>
}