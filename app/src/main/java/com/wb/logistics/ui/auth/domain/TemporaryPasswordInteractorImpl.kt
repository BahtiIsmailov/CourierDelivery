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
    private val repository: AuthRemoteRepository,
) : TemporaryPasswordInteractor {
    private val timerStates: BehaviorSubject<TimerState>
    private var timerDisposable: Disposable? = null

    override fun sendTmpPassword(phone: String): Single<RemainingAttemptsResponse> {
        return repository.sendTmpPassword(phone).compose(rxSchedulerFactory.applySingleSchedulers())
    }

    override fun checkPassword(phone: String, tmpPassword: String): Completable {
        return repository.passwordCheck(phone, tmpPassword)
            .compose(rxSchedulerFactory.applyCompletableSchedulers())
    }

    private var durationTime = 0
    override fun startTimer(durationTime: Int) {
        this.durationTime = durationTime
        if (timerDisposable == null) {
            timerDisposable = Observable.interval(PERIOD, TimeUnit.MILLISECONDS)
                .subscribe({ onTimeConfirmCode(it) }) { }
        }
    }

    override val timer: Flowable<TimerState>
        get() = timerStates.compose(rxSchedulerFactory.applyObservableSchedulers())
            .toFlowable(BackpressureStrategy.BUFFER)

    override fun stopTimer() {
        timeConfirmCodeDisposable()
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

    private fun timeConfirmCodeDisposable() {
        if (timerDisposable != null) {
            timerDisposable!!.dispose()
            timerDisposable = null
        }
    }

    private fun publishCallState(timerState: TimerState) {
        timerStates.onNext(timerState)
    }

    override fun passwordChanges(observable: InitialValueObservable<CharSequence>): Observable<Boolean> {
        return observable.map { it.toString() }
            .distinctUntilChanged()
            .map { it.length >= LENGTH_SMS_CODE }
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    companion object {
        private const val PERIOD = 1000L
        private const val LENGTH_SMS_CODE = 4
    }

    init {
        timerStates = BehaviorSubject.create()
    }
}