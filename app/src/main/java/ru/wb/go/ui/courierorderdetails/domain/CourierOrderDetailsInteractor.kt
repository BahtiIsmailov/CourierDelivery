package ru.wb.go.ui.courierorderdetails.domain

import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import ru.wb.go.db.entity.courierlocal.CourierOrderLocalDataEntity
import ru.wb.go.ui.couriermap.CourierMapAction
import ru.wb.go.ui.couriermap.CourierMapState

interface CourierOrderDetailsInteractor {

    fun observeOrderData(): Flowable<CourierOrderLocalDataEntity>

    fun carNumberIsConfirm(): Boolean

    fun carNumber(): String

    fun observeMapAction(): Observable<CourierMapAction>

    fun mapState(state: CourierMapState)

    fun anchorTask(): Single<AnchorTaskStatus>

    fun isDemoMode(): Boolean

}