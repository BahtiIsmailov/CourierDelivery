package ru.wb.perevozka.ui.courierorderdetails.domain

import io.reactivex.Flowable
import io.reactivex.Single
import ru.wb.perevozka.db.entity.courierlocal.CourierOrderLocalDataEntity
import ru.wb.perevozka.network.api.app.entity.CourierAnchorEntity

interface CourierOrderDetailsInteractor {

    fun observeOrderData(): Flowable<CourierOrderLocalDataEntity>

    fun carNumberIsConfirm(): Boolean

}