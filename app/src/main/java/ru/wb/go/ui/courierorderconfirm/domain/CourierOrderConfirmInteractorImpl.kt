package ru.wb.go.ui.courierorderconfirm.domain

import android.annotation.SuppressLint
import io.reactivex.*
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import ru.wb.go.db.CourierLocalRepository
import ru.wb.go.db.entity.TaskStatus
import ru.wb.go.db.entity.courierlocal.CourierOrderLocalDataEntity
import ru.wb.go.db.entity.courierlocal.LocalOrderEntity
import ru.wb.go.network.api.app.AppRemoteRepository
import ru.wb.go.network.monitor.NetworkMonitorRepository
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.network.rx.RxSchedulerFactory
import ru.wb.go.network.token.UserManager
import ru.wb.go.ui.auth.signup.TimerOverStateImpl
import ru.wb.go.ui.auth.signup.TimerState
import ru.wb.go.ui.auth.signup.TimerStateImpl
import ru.wb.go.utils.managers.TimeManager
import java.util.concurrent.TimeUnit

class CourierOrderConfirmInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val networkMonitorRepository: NetworkMonitorRepository,
    private val appRemoteRepository: AppRemoteRepository,
    private val locRepo: CourierLocalRepository,
    private val userManager: UserManager,
    private val timeManager: TimeManager,
) : CourierOrderConfirmInteractor {

    private val timerStates: BehaviorSubject<TimerState> = BehaviorSubject.create()
    private var timerDisposable: Disposable? = null
    private var durationTime = 0


    @SuppressLint("SimpleDateFormat")
    override fun anchorTask(): Completable {

        val reservedTime = timeManager.getLocalTime()

        val order = locRepo.orderData()!!

        return appRemoteRepository.anchorTask(
            order.courierOrderLocalEntity.id.toString(),
            userManager.carNumber()
        )
            .doOnComplete {
                val wh = locRepo.readCurrentWarehouse().blockingGet()
                val ro =
                    with(order.courierOrderLocalEntity) {
                        LocalOrderEntity(
                            orderId = id,
                            routeID = routeID,
                            gate = gate,
                            minPrice = minPrice,
                            minVolume = minVolume,
                            minBoxes = minBoxesCount,
                            countOffices = order.dstOffices.size,
                            wbUserID = -1,
                            carNumber = userManager.carNumber(),
                            reservedAt = reservedTime,
                            startedAt = "",
                            reservedDuration = reservedDuration,
                            status = TaskStatus.TIMER.status,
                            cost = 0,
                            srcId = wh.id,
                            srcName = wh.name,
                            srcAddress = wh.fullAddress,
                            srcLongitude = wh.longitude,
                            srcLatitude = wh.latitude,
                        )
                    }
                locRepo.setOrderInReserve(ro)

            }
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
        return locRepo.observeOrderData()
            .compose(rxSchedulerFactory.applyFlowableSchedulers())
    }

    override fun observeNetworkConnected(): Observable<NetworkState> {
        return networkMonitorRepository.networkConnected()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

}