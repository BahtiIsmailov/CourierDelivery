package ru.wb.go.ui.courierorderconfirm.domain

import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import ru.wb.go.db.CourierLocalRepository
import ru.wb.go.db.entity.TaskStatus
import ru.wb.go.db.entity.courierlocal.CourierOrderLocalDataEntity
import ru.wb.go.network.api.app.AppRemoteRepository
import ru.wb.go.network.rx.RxSchedulerFactory
import ru.wb.go.network.token.UserManager
import ru.wb.go.ui.auth.signup.TimerOverStateImpl
import ru.wb.go.ui.auth.signup.TimerState
import ru.wb.go.ui.auth.signup.TimerStateImpl
import java.util.concurrent.TimeUnit

class CourierOrderConfirmInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val appRemoteRepository: AppRemoteRepository,
    private val courierLocalRepository: CourierLocalRepository,
    private val userManager: UserManager
) : CourierOrderConfirmInteractor {

    private val timerStates: BehaviorSubject<TimerState> = BehaviorSubject.create()
    private var timerDisposable: Disposable? = null
    private var durationTime = 0

    override fun anchorTask(): Completable {
        //return Completable.error(BadRequestException(Error("Error", "500", null)))
//        return Completable.error(ExceptionInInitializerError())
//            .compose(rxSchedulerFactory.applyCompletableSchedulers())
//        return Completable.timer(1000, TimeUnit.MILLISECONDS)
//            .andThen(Completable.error(BadRequestException(Error("Error", "500", null))))
//            .compose(rxSchedulerFactory.applyCompletableSchedulers())

        return courierLocalRepository.observeOrderData()
            .map { it.courierOrderLocalEntity.id }
            .map { it.toString() }
            .firstOrError()
            .flatMapCompletable { appRemoteRepository.anchorTask(it, userManager.carNumber()) }
            .doOnComplete { userManager.saveStatusTask(TaskStatus.TIMER.status) }
            .compose(rxSchedulerFactory.applyCompletableSchedulers())
    }

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

    override fun observeOrderData(): Flowable<CourierOrderLocalDataEntity> {
        return courierLocalRepository.observeOrderData()
            .compose(rxSchedulerFactory.applyFlowableSchedulers())
    }

}