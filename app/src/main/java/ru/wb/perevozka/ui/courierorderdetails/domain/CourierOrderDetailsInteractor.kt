package ru.wb.perevozka.ui.courierorderdetails.domain

import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import ru.wb.perevozka.db.entity.courierlocal.CourierOrderLocalDataEntity
import ru.wb.perevozka.network.api.app.entity.CourierAnchorEntity
import ru.wb.perevozka.ui.couriermap.CourierMapAction
import ru.wb.perevozka.ui.couriermap.CourierMapState

interface CourierOrderDetailsInteractor {

    fun observeOrderData(): Flowable<CourierOrderLocalDataEntity>

    fun carNumberIsConfirm(): Boolean

    fun observeMapAction(): Observable<CourierMapAction>

    fun mapState(state: CourierMapState)

}