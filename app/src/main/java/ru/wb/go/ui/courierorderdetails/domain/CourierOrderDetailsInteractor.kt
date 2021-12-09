package ru.wb.go.ui.courierorderdetails.domain

import io.reactivex.Flowable
import io.reactivex.Observable
import ru.wb.go.db.entity.courierlocal.CourierOrderLocalDataEntity
import ru.wb.go.ui.couriermap.CourierMapAction
import ru.wb.go.ui.couriermap.CourierMapState

interface CourierOrderDetailsInteractor {

    fun observeOrderData(): Flowable<CourierOrderLocalDataEntity>

    fun carNumberIsConfirm(): Boolean

    fun observeMapAction(): Observable<CourierMapAction>

    fun mapState(state: CourierMapState)

}