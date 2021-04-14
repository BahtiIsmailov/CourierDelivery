package com.wb.logistics.ui.auth.domain

import com.jakewharton.rxbinding3.InitialValueObservable
import com.wb.logistics.network.api.auth.AuthRepository
import com.wb.logistics.network.rx.RxSchedulerFactory
import io.reactivex.Completable
import io.reactivex.Observable

class InputPasswordInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val authRepository: AuthRepository,
//    private val appRepository: AppRepository,
//    private val userManager: UserManager,
) : InputPasswordInteractor {
    override fun authByPassword(phone: String, password: String): Completable {
        return authRepository.authByPhoneOrPassword(phone, password, false)
            //.andThen(checkAndRefreshUser(phone))
            .compose(rxSchedulerFactory.applyCompletableSchedulers())
    }

/*    private fun checkAndRefreshUser(phone: String): Completable {
        return Single.just(userManager)
            .filter { userManager.isUserChanged(phone) }
            .doOnSuccess { it.savePhone(phone) }
            .flatMapCompletable {
                Completable.fromCallable {
                    appRepository.deleteAllFlightData()
                    appRepository.deleteAllFlightBox()
                    appRepository.deleteAllMatchingBox()
                    appRepository.deleteAllFlightBoxScanned()
                    appRepository.deleteAllFlightBoxBalanceAwait()
                }
            }
    }*/

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