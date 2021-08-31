package ru.wb.perevozka.ui.courierorderconferm.domain

import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import ru.wb.perevozka.db.CourierLocalRepository
import ru.wb.perevozka.db.entity.courierlocal.CourierOrderLocalDataEntity
import ru.wb.perevozka.network.api.app.AppRemoteRepository
import ru.wb.perevozka.network.api.app.entity.CourierAnchorEntity
import ru.wb.perevozka.network.rx.RxSchedulerFactory
import ru.wb.perevozka.network.token.UserManager
import ru.wb.perevozka.ui.auth.signup.TimerOverStateImpl
import ru.wb.perevozka.ui.auth.signup.TimerState
import ru.wb.perevozka.ui.auth.signup.TimerStateImpl
import java.util.concurrent.TimeUnit

class CourierOrderConfirmInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val appRemoteRepository: AppRemoteRepository,
    private val courierLocalRepository: CourierLocalRepository,
    private val userManager: UserManager
) : CourierOrderConfirmInteractor {

    private val timerStates: BehaviorSubject<TimerState> = BehaviorSubject.create()
    private var timerDisposable: Disposable? = null


    override fun anchorTask(taskID: String): Single<CourierAnchorEntity> {
        return appRemoteRepository.anchorTask(taskID)
            .compose(rxSchedulerFactory.applySingleSchedulers())
    }

    private var durationTime = 0
    override fun startTimer(durationTime: Int) {
        this.durationTime = durationTime
        if (timerDisposable == null) {
            timerDisposable = Observable.interval(1000L, TimeUnit.MILLISECONDS)
                .subscribe({ onTimeConfirmCode(it) }) { }
        }
    }

    override val timer: Flowable<TimerState>
        get() = timerStates.compose(rxSchedulerFactory.applyObservableSchedulers())
            .toFlowable(BackpressureStrategy.BUFFER)

    override fun stopTimer() {
        timeConfirmCodeDisposable()
    }

    override fun carNumber(): String {
        return userManager.carNumber()
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

    override fun observeOrderData(): Flowable<CourierOrderLocalDataEntity> {
        return courierLocalRepository.observeOrderData()
            .compose(rxSchedulerFactory.applyFlowableSchedulers())
    }

}