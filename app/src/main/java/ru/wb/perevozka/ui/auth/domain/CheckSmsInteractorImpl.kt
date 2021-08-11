package ru.wb.perevozka.ui.auth.domain

import com.jakewharton.rxbinding3.InitialValueObservable
import io.reactivex.*
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import ru.wb.perevozka.app.NEED_APPROVE_COURIER_DOCUMENTS
import ru.wb.perevozka.app.NEED_SEND_COURIER_DOCUMENTS
import ru.wb.perevozka.network.api.auth.AuthRemoteRepository
import ru.wb.perevozka.network.monitor.NetworkMonitorRepository
import ru.wb.perevozka.network.monitor.NetworkState
import ru.wb.perevozka.network.rx.RxSchedulerFactory
import ru.wb.perevozka.network.token.TokenManager
import ru.wb.perevozka.ui.auth.signup.TimerOverStateImpl
import ru.wb.perevozka.ui.auth.signup.TimerState
import ru.wb.perevozka.ui.auth.signup.TimerStateImpl
import java.util.concurrent.TimeUnit

class CheckSmsInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val networkMonitorRepository: NetworkMonitorRepository,
    private val authRepository: AuthRemoteRepository,
    private val tokenManager: TokenManager
) : CheckSmsInteractor {

    private val timerStates: BehaviorSubject<TimerState> = BehaviorSubject.create()
    private var timerDisposable: Disposable? = null

    override fun remindPasswordChanges(observable: InitialValueObservable<CharSequence>): Observable<Boolean> {
        return observable.map { it.toString() }
            .distinctUntilChanged()
            .map { it.length >= LENGTH_PASSWORD_MIN }
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    private var durationTime = 0
    override fun startTimer(durationTime: Int) {
        this.durationTime = durationTime
        if (timerDisposable == null) {
            timerDisposable = Observable.interval(PERIOD, TimeUnit.MILLISECONDS)
                .subscribe({ onTimeConfirmCode(it) }) { }
        }
    }

    private fun onTimeConfirmCode(tick: Long) {
        if (tick > durationTime) {
            timeConfirmCodeDisposable()
            publishCallState(TimerOverStateImpl())
        } else {
            val counterTick = durationTime - tick.toInt()
            publishCallState(TimerStateImpl(counterTick))
        }
    }

    private fun publishCallState(timerState: TimerState) {
        timerStates.onNext(timerState)
    }

    private fun timeConfirmCodeDisposable() {
        if (timerDisposable != null) {
            timerDisposable!!.dispose()
            timerDisposable = null
        }
    }

    override val timer: Flowable<TimerState>
        get() = timerStates.compose(rxSchedulerFactory.applyObservableSchedulers())
            .toFlowable(BackpressureStrategy.BUFFER)

    override fun stopTimer() {
        timeConfirmCodeDisposable()
    }

    override fun observeNetworkConnected(): Observable<NetworkState> {
        return networkMonitorRepository.networkConnected()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    override fun auth(phone: String, password: String): Single<CheckSmsData> {
        val auth = authRepository.auth(phone, password, true)
        val tokenResources = Single.fromCallable { tokenManager.resources() }
        return auth.andThen(tokenResources)
            .map {
                when {
                    it.contains(NEED_SEND_COURIER_DOCUMENTS) -> CheckSmsData.NeedSendCourierDocument
                    it.contains(NEED_APPROVE_COURIER_DOCUMENTS) -> CheckSmsData.NeedApproveCourierDocuments
                    else -> CheckSmsData.UserRegistered
                }
            }
            .compose(rxSchedulerFactory.applySingleSchedulers())
    }

    override fun couriersExistAndSavePhone(phone: String): Completable {
        return authRepository.couriersExistAndSavePhone(phone)
            .compose(rxSchedulerFactory.applyCompletableSchedulers())
    }

    companion object {
        private const val PERIOD = 1000L
        private const val LENGTH_PASSWORD_MIN = 4
    }
}