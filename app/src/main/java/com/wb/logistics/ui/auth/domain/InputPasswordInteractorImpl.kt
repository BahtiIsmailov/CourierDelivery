package com.wb.logistics.ui.auth.domain

import com.jakewharton.rxbinding3.InitialValueObservable
import com.wb.logistics.network.api.auth.AuthRepository
import com.wb.logistics.network.rx.RxSchedulerFactory
import io.reactivex.Completable
import io.reactivex.Observable

class InputPasswordInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val repository: AuthRepository
) : InputPasswordInteractor {
    override fun authByPassword(phone: String, password: String): Completable {
        return repository.authByPhoneOrPassword(phone, password, false)
    }

    override fun remindPasswordChanges(observable: InitialValueObservable<CharSequence>): Observable<Boolean> {
        return observable.map { it.toString() }
            .distinctUntilChanged()
            .map { it.length >= LENGTH_PASSWORD_MIN }
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    companion object {
        private const val LENGTH_PASSWORD_MIN = 1
    }
}