package ru.wb.go.ui.auth.domain

import com.jakewharton.rxbinding3.InitialValueObservable
import io.reactivex.*
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import ru.wb.go.app.NEED_APPROVE_COURIER_DOCUMENTS
import ru.wb.go.app.NEED_SEND_COURIER_DOCUMENTS
import ru.wb.go.network.api.auth.AuthRemoteRepository
import ru.wb.go.network.monitor.NetworkMonitorRepository
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.network.rx.RxSchedulerFactory
import ru.wb.go.network.token.TokenManager
import ru.wb.go.ui.auth.signup.TimerOverStateImpl
import ru.wb.go.ui.auth.signup.TimerState
import ru.wb.go.ui.auth.signup.TimerStateImpl
import java.util.concurrent.TimeUnit

class CheckSmsInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val networkMonitorRepository: NetworkMonitorRepository,
    private val authRepository: AuthRemoteRepository,
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
            publishCallState(TimerStateImpl(durationTime, counterTick))
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

    override fun auth(phone: String, password: String): Completable {
        val auth = authRepository.auth(phone, password, true)
        return auth.compose(rxSchedulerFactory.applyCompletableSchedulers())
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