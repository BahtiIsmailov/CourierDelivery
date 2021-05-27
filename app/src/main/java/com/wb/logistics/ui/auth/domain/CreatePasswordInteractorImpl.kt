package com.wb.logistics.ui.auth.domain

import com.jakewharton.rxbinding3.InitialValueObservable
import com.wb.logistics.network.api.auth.AuthRemoteRepository
import com.wb.logistics.network.rx.RxSchedulerFactory
import io.reactivex.Completable
import io.reactivex.Observable

class CreatePasswordInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val repository: AuthRemoteRepository
) : CreatePasswordInteractor {
    override fun remindPasswordChanges(observable: InitialValueObservable<CharSequence>): Observable<Boolean> {
        return observable.map { it.toString() }
            .distinctUntilChanged()
            .map { it.length >= LENGTH_PASSWORD_MIN }
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    override fun saveAndAuthByPassword(
        phone: String,
        password: String,
        tmpPassword: String
    ): Completable {
        val changePassword = repository.changePasswordBySmsCode(phone, password, tmpPassword)
        val authByPassword = repository.authByPhoneOrPassword(phone, password, false)
        return changePassword.andThen(authByPassword)
    }

    companion object {
        private const val LENGTH_PASSWORD_MIN = 1
    }
}