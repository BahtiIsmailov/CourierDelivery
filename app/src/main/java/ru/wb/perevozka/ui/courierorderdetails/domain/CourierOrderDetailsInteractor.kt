package ru.wb.perevozka.ui.courierorderdetails.domain

import io.reactivex.Single
import ru.wb.perevozka.network.api.app.entity.CourierAnchorEntity

interface CourierOrderDetailsInteractor {

    fun anchorTask(taskID: String): Single<CourierAnchorEntity>

}