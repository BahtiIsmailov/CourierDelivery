package com.wb.logistics.ui.auth.domain

import com.jakewharton.rxbinding3.InitialValueObservable
import com.wb.logistics.network.api.auth.AuthRemoteRepository
import com.wb.logistics.network.api.auth.response.RemainingAttemptsResponse
import com.wb.logistics.network.rx.RxSchedulerFactory
import com.wb.logistics.ui.auth.signup.TimerOverStateImpl
import com.wb.logistics.ui.auth.signup.TimerState
import com.wb.logistics.ui.auth.signup.TimerStateImpl
import io.reactivex.*
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit

class TemporaryPasswordInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val repository: AuthRemoteRepository
) : TemporaryPasswordInteractor {
    private val timerStates: BehaviorSubject<TimerState>
    private var timerDisposable: Disposable? = null
    private var counterCheckAttempt = 0
    private var countCheckAttempt = 0
    override fun sendTmpPassword(phone: String): Single<RemainingAttemptsResponse> {
        return repository.sendTmpPassword(phone)
    }

    override fun checkPassword(phone: String, tmpPassword: String): Completable {
        upCounterCheckSms()
        return repository.passwordCheck(phone, tmpPassword)
            .compose(rxSchedulerFactory.applyCompletableSchedulers())
    }

    private fun upCounterCheckSms() {
        countCheckAttempt = COUNT_CHECK_ATTEMPT - ++counterCheckAttempt
        if (countCheckAttempt <= 0) clearCountCheckAttempt()
    }

    override fun startTimer() {
        if (timerDisposable == null) {
            timerDisposable = Observable.interval(PERIOD.toLong(), TimeUnit.MILLISECONDS)
                .subscribe({ onTimeConfirmCode(it) }) { throwable: Throwable? -> }
        }
    }

    override val timer: Flowable<TimerState>
        get() = timerStates.compose(rxSchedulerFactory.applyObservableSchedulers())
            .toFlowable(BackpressureStrategy.BUFFER)

    override fun stopTimer() {
        timeConfirmCodeDisposable()
    }

    private fun onTimeConfirmCode(tick: Long) {
        if (tick > DURATION_CODE) {
            timeConfirmCodeDisposable()
            publishCallState(TimerOverStateImpl())
        } else {
            val counterTick = DURATION_CODE - tick.toInt()
            publishCallState(TimerStateImpl(counterTick))
        }
    }

    private fun timeConfirmCodeDisposable() {
        if (timerDisposable != null) {
            timerDisposable!!.dispose()
            timerDisposable = null
        }
    }

    private fun publishCallState(timerState: TimerState) {
        timerStates.onNext(timerState)
    }

    override fun clearCountCheckAttempt() {
        counterCheckAttempt = 0
    }

    override fun passwordChanges(observable: InitialValueObservable<CharSequence>): Observable<Boolean> {
        return observable.map { it.toString() }
            .distinctUntilChanged()
            .map { it.length == LENGTH_SMS_CODE }
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    companion object {
        private const val PERIOD = 1000
        private const val DURATION_CODE = 30
        private const val COUNT_CHECK_ATTEMPT = 3
        private const val LENGTH_SMS_CODE = 4
    }

    init {
        timerStates = BehaviorSubject.create()
    }
}