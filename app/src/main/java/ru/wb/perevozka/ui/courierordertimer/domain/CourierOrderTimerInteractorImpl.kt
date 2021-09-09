package ru.wb.perevozka.ui.courierordertimer.domain

import io.reactivex.*
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import ru.wb.perevozka.db.CourierLocalRepository
import ru.wb.perevozka.db.entity.courierlocal.CourierOrderLocalDataEntity
import ru.wb.perevozka.network.api.app.AppRemoteRepository
import ru.wb.perevozka.network.api.app.entity.CourierAnchorEntity
import ru.wb.perevozka.network.rx.RxSchedulerFactory
import ru.wb.perevozka.ui.auth.signup.TimerOverStateImpl
import ru.wb.perevozka.ui.auth.signup.TimerState
import ru.wb.perevozka.ui.auth.signup.TimerStateImpl
import java.util.concurrent.TimeUnit

class CourierOrderTimerInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val appRemoteRepository: AppRemoteRepository,
    private val courierLocalRepository: CourierLocalRepository
) : CourierOrderTimerInteractor {

    private val timerStates: BehaviorSubject<TimerState> = BehaviorSubject.create()
    private var timerDisposable: Disposable? = null

    override fun anchorTask(): Single<CourierAnchorEntity> {
        return taskId().flatMap { appRemoteRepository.anchorTask(it) }
    }

    override fun deleteTask(): Completable {
        return taskId().flatMapCompletable { appRemoteRepository.deleteTask(it) }
            .compose(rxSchedulerFactory.applyCompletableSchedulers())
    }

    private fun taskId() =
        courierLocalRepository.observeOrderData()
            .map { it.courierOrderLocalEntity.id.toString() }
            .first("")

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