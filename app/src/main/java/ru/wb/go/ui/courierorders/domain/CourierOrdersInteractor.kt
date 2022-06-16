package ru.wb.go.ui.courierorders.domain

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.coroutines.flow.Flow
import ru.wb.go.db.entity.courierlocal.CourierOrderLocalDataEntity
import ru.wb.go.ui.BaseServiceInteractor
import ru.wb.go.ui.couriermap.CourierMapAction
import ru.wb.go.ui.couriermap.CourierMapState

interface CourierOrdersInteractor : BaseServiceInteractor {

    suspend fun freeOrdersLocalClearAndSave(srcOfficeID: Int): List<CourierOrderLocalDataEntity>

    fun freeOrdersLocal():  Flow<List<CourierOrderLocalDataEntity>>

    suspend fun saveRowOrder(rowOrder: Int)

     fun selectedOrder(rowOrder: Int):  CourierOrderLocalDataEntity

      fun selectedRowOrder(): Int

      fun mapState(state: CourierMapState)

    fun observeMapAction(): Flow<CourierMapAction>

      fun carNumberIsConfirm(): Boolean

      fun isDemoMode(): Boolean

      fun carNumber(): String

    suspend fun carType(): Int

    suspend fun anchorTask()

}