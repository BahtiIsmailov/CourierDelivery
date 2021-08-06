package ru.wb.perevozka.ui.auth.domain

import com.jakewharton.rxbinding3.InitialValueObservable
import ru.wb.perevozka.network.api.auth.AuthRemoteRepository
import ru.wb.perevozka.network.monitor.NetworkMonitorRepository
import ru.wb.perevozka.network.monitor.NetworkState
import ru.wb.perevozka.network.rx.RxSchedulerFactory
import io.reactivex.Completable
import io.reactivex.Observable

class CreatePasswordInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val networkMonitorRepository: NetworkMonitorRepository,
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
        return changePassword.andThen(authByPassword).compose(rxSchedulerFactory.applyCompletableSchedulers())
    }

    override fun observeNetworkConnected(): Observable<NetworkState> {
        return networkMonitorRepository.networkConnected()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    companion object {
        private const val LENGTH_PASSWORD_MIN = 1
    }
}