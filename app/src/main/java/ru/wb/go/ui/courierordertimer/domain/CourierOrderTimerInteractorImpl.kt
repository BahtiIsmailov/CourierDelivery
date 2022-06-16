package ru.wb.go.ui.courierordertimer.domain

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import ru.wb.go.app.DEFAULT_ARRIVAL_TIME_COURIER_MIN
import ru.wb.go.db.CourierLocalRepository
import ru.wb.go.db.TaskTimerRepository
import ru.wb.go.db.entity.courierlocal.CourierOrderLocalDataEntity
import ru.wb.go.db.entity.courierlocal.CourierTimerEntity
import ru.wb.go.network.api.app.AppRemoteRepository
import ru.wb.go.network.rx.RxSchedulerFactory
import ru.wb.go.ui.auth.signup.TimerState
import ru.wb.go.utils.managers.TimeManager
import ru.wb.go.utils.time.TimeFormatter

class CourierOrderTimerInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val appRemoteRepository: AppRemoteRepository,
    private val locRepo: CourierLocalRepository,
    private val taskTimerRepository: TaskTimerRepository,
    private val timeFormatter: TimeFormatter,
    private val timeManager: TimeManager,
) : CourierOrderTimerInteractor {

    override suspend fun deleteTask() {
        taskTimerRepository.stopTimer()
        return withContext(Dispatchers.IO) {
            appRemoteRepository.deleteTask(locRepo.getOrderId())
            locRepo.deleteOrder()
        }
    }

    override suspend fun startTimer(reservedDuration: String, reservedAt: String) {
        var arrivalSec: Long
        val durationSec = (reservedDuration.toIntOrNull() ?: DEFAULT_ARRIVAL_TIME_COURIER_MIN) * 60L
        arrivalSec = if (reservedAt.isEmpty()) {
            durationSec
        } else {
            durationSec - timeManager.getPassedTime(reservedAt)
        }

        if (arrivalSec < 0 || arrivalSec > durationSec) arrivalSec = 0L

        return taskTimerRepository.startTimer(durationSec.toInt(), arrivalSec.toInt())
    }


    override val timer: Flow<TimerState>
        get() = taskTimerRepository.timer

    override suspend fun stopTimer() {
        taskTimerRepository.stopTimer()
    }

    override suspend fun observeOrderData(): CourierOrderLocalDataEntity {
        return withContext(Dispatchers.IO){
            locRepo.observeOrderData()
        }
    }

    override suspend fun timerEntity(): CourierTimerEntity {
        return withContext(Dispatchers.IO) {
            val it = locRepo.getOrder()
            CourierTimerEntity(
                it.srcName, it.orderId, it.minPrice, it.minBoxes, it.minVolume,
                it.countOffices, it.gate, it.reservedDuration, it.reservedAt
            )
        }
    }

}

/*
  override fun deleteTask(): Completable {
        taskTimerRepository.stopTimer()
        return locRepo.getOrderId()
            .flatMapCompletable { appRemoteRepository.deleteTask(it) }
            .doOnComplete {
                locRepo.deleteOrder()
            }
            .compose(rxSchedulerFactory.applyCompletableSchedulers())
    }

    override fun startTimer(reservedDuration: String, reservedAt: String) {
        var arrivalSec: Long
        val durationSec = (reservedDuration.toIntOrNull() ?: DEFAULT_ARRIVAL_TIME_COURIER_MIN) * 60L
        arrivalSec = if (reservedAt.isEmpty()) {
            durationSec
        } else {
            durationSec - timeManager.getPassedTime(reservedAt)
        }

        if (arrivalSec < 0 || arrivalSec > durationSec) arrivalSec = 0L

        return taskTimerRepository.startTimer(durationSec.toInt(), arrivalSec.toInt())
    }

    override val timer: Flowable<TimerState>
        get() = taskTimerRepository.timer.compose(rxSchedulerFactory.applyFlowableSchedulers())

    override fun stopTimer() {
        taskTimerRepository.stopTimer()
    }

    override fun observeOrderData(): Flowable<CourierOrderLocalDataEntity> {
        return locRepo.observeOrderData()
            .compose(rxSchedulerFactory.applyFlowableSchedulers())
    }

    override fun timerEntity(): Single<CourierTimerEntity> {

        return Single.just(locRepo.getOrder())
            .map {
                CourierTimerEntity(
                    it.srcName, it.orderId, it.minPrice, it.minBoxes, it.minVolume,
                    it.countOffices, it.gate, it.reservedDuration, it.reservedAt
                )
            }
            .compose(rxSchedulerFactory.applySingleSchedulers())
    }


 */