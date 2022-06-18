package ru.wb.go.ui.courierorders.domain

import kotlinx.coroutines.flow.Flow
import ru.wb.go.db.entity.courierlocal.CourierOrderLocalDataEntity
import ru.wb.go.ui.BaseServiceInteractor
import ru.wb.go.ui.couriermap.CourierMapAction
import ru.wb.go.ui.couriermap.CourierMapState

interface CourierOrdersInteractor : BaseServiceInteractor {

    suspend fun freeOrdersLocalClearAndSave(srcOfficeID: Int): List<CourierOrderLocalDataEntity>

    fun freeOrdersLocal(): Flow<List<CourierOrderLocalDataEntity>>

    fun saveRowOrder(rowOrder: Int)

    suspend fun selectedOrder(rowOrder: Int): CourierOrderLocalDataEntity

    fun selectedRowOrder(): Int

    fun mapState(state: CourierMapState)

    fun observeMapAction(): Flow<CourierMapAction>

    fun carNumberIsConfirm(): Boolean

    fun isDemoMode(): Boolean

    fun carNumber(): String

      fun carType(): Int

    suspend fun anchorTask()

}

/*
interface CourierOrdersInteractor : BaseServiceInteractor {

    fun freeOrdersLocalClearAndSave(srcOfficeID: Int): Single<MutableList<CourierOrderLocalDataEntity>>

    fun freeOrdersLocal(): Single<MutableList<CourierOrderLocalDataEntity>>

    fun saveRowOrder(rowOrder: Int)

    fun selectedOrder(rowOrder: Int): Single<CourierOrderLocalDataEntity>

    fun selectedRowOrder(): Int

    fun mapState(state: CourierMapState)

    fun observeMapAction(): Observable<CourierMapAction>

    fun carNumberIsConfirm(): Boolean

    fun isDemoMode(): Boolean

    fun carNumber(): String

    fun carType(): Int

    fun anchorTask(): Completable

}
 */