package ru.wb.go.ui.courierordertimer.domain

import kotlinx.coroutines.flow.Flow
import ru.wb.go.app.DEFAULT_ARRIVAL_TIME_COURIER_MIN
import ru.wb.go.db.CourierLocalRepository
import ru.wb.go.db.TaskTimerRepository
import ru.wb.go.db.entity.courierlocal.CourierOrderLocalDataEntity
import ru.wb.go.db.entity.courierlocal.CourierTimerEntity
import ru.wb.go.network.api.app.AppRemoteRepository
import ru.wb.go.ui.auth.signup.TimerState
import ru.wb.go.utils.managers.TimeManager

class CourierOrderTimerInteractorImpl(
    private val appRemoteRepository: AppRemoteRepository,
    private val locRepo: CourierLocalRepository,
    private val taskTimerRepository: TaskTimerRepository,
    private val timeManager: TimeManager,
) : CourierOrderTimerInteractor {

    override suspend fun deleteTask() {
        taskTimerRepository.stopTimer()
        appRemoteRepository.deleteTask(locRepo.getOrderId())
        locRepo.deleteOrder()
    }

    override fun startTimer(reservedDuration: String, reservedAt: String) { // arrival time = 0
        var arrivalSec: Long
        val durationSec = (reservedDuration.toIntOrNull() ?: DEFAULT_ARRIVAL_TIME_COURIER_MIN) * 60L
        arrivalSec = if (reservedAt.isEmpty()) {
            durationSec
        } else {
            durationSec - timeManager.getPassedTime(reservedAt)
        }

        if (arrivalSec < 0 || arrivalSec > durationSec) {
            arrivalSec = 0L // приходит arrivalSec > durationSec когда часовой пояс другой
        }

        return taskTimerRepository.startTimer(durationSec.toInt(), arrivalSec.toInt())
    }

    override val timer: Flow<TimerState>
        get() = taskTimerRepository.timer

    override suspend fun stopTimer() {
        taskTimerRepository.stopTimer()
    }

    override fun observeOrderData(): Flow<CourierOrderLocalDataEntity> {
        return locRepo.observeOrderData()
    }

    override suspend fun timerEntity(): CourierTimerEntity {
        var it = locRepo.getOrder()
        if (it == null){
            it = appRemoteRepository.tasksMy().order
        }
        return CourierTimerEntity(
            it.route, it.srcName, it.orderId, it.minCost, it.minBoxes, it.minVolume,
            it.countOffices, it.gate, it.reservedDuration, it.reservedAt
        )

    }

}

