package ru.wb.go.ui.courierordertimer.domain

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import ru.wb.go.app.DEFAULT_ARRIVAL_TIME_COURIER_MIN
import ru.wb.go.db.CourierLocalRepository
import ru.wb.go.db.TaskTimerRepository
import ru.wb.go.db.entity.TaskStatus
import ru.wb.go.db.entity.courierlocal.CourierOrderLocalDataEntity
import ru.wb.go.db.entity.courierlocal.CourierTimerEntity
import ru.wb.go.network.api.app.AppRemoteRepository
import ru.wb.go.network.rx.RxSchedulerFactory
import ru.wb.go.network.token.UserManager
import ru.wb.go.ui.auth.signup.TimerState
import ru.wb.go.utils.time.TimeFormatter

class CourierOrderTimerInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val appRemoteRepository: AppRemoteRepository,
    private val courierLocalRepository: CourierLocalRepository,
    private val taskTimerRepository: TaskTimerRepository,
    private val timeFormatter: TimeFormatter,
    private val userManager: UserManager
) : CourierOrderTimerInteractor {

    override fun deleteTask(): Completable {
        return taskId().flatMapCompletable { appRemoteRepository.deleteTask(it) }
            .doOnComplete {
                taskTimerRepository.stopTimer()
                userManager.saveStatusTask(TaskStatus.EMPTY.status)
            }
            .compose(rxSchedulerFactory.applyCompletableSchedulers())
    }

    override fun startTimer(reservedDuration: String, reservedAt: String) {
        var arrivalSec: Long
        val durationSec = (reservedDuration.toIntOrNull() ?: DEFAULT_ARRIVAL_TIME_COURIER_MIN) * 60L
        arrivalSec = if (reservedAt.isEmpty()) {
            durationSec
        } else {
            val reservedAtDataTime =
                timeFormatter.dateTimeWithoutTimezoneFromString(reservedAt).millis
            val currentDateTime = timeFormatter.currentDateTime().millis
            val offsetSec = (currentDateTime - reservedAtDataTime) / 1000
            durationSec - offsetSec
        }
        if (arrivalSec < 0) arrivalSec = 0L

        return taskTimerRepository.startTimer(durationSec.toInt(), arrivalSec.toInt())
    }

    override val timer: Flowable<TimerState>
        get() = taskTimerRepository.timer.compose(rxSchedulerFactory.applyFlowableSchedulers())

    override fun stopTimer() {
        taskTimerRepository.stopTimer()
    }

    private fun taskId() =
        courierLocalRepository.observeOrderData()
            .map { it.courierOrderLocalEntity.id.toString() }
            .first("")


    override fun observeOrderData(): Flowable<CourierOrderLocalDataEntity> {
        return courierLocalRepository.observeOrderData()
            .compose(rxSchedulerFactory.applyFlowableSchedulers())
    }

    override fun timerEntity(): Single<CourierTimerEntity> {
        return courierLocalRepository.courierTimerEntity()
            .compose(rxSchedulerFactory.applySingleSchedulers())
    }

}