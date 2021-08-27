package ru.wb.perevozka.ui.courierordertimer.domain

import io.reactivex.Single
import ru.wb.perevozka.network.api.app.entity.CourierAnchorEntity

interface CourierOrderTimerInteractor {

    fun anchorTask(taskID: String): Single<CourierAnchorEntity>

}