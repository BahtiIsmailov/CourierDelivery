package ru.wb.go.ui.courierorders.domain

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import ru.wb.go.db.entity.courierlocal.CourierOrderLocalDataEntity
import ru.wb.go.ui.BaseServiceInteractor
import ru.wb.go.ui.couriermap.CourierMapAction
import ru.wb.go.ui.couriermap.CourierMapState

interface CourierOrdersInteractor : BaseServiceInteractor {

    suspend fun freeOrdersLocalClearAndSave(srcOfficeID: Int): List<CourierOrderLocalDataEntity>

    suspend fun freeOrdersLocal():  List<CourierOrderLocalDataEntity>

    fun saveRowOrder(rowOrder: Int)

    suspend fun selectedOrder(rowOrder: Int):  CourierOrderLocalDataEntity

    suspend fun selectedRowOrder(): Int

    fun mapState(state: CourierMapState)

    suspend fun observeMapAction(): CourierMapAction

    fun carNumberIsConfirm(): Boolean

    fun isDemoMode(): Boolean

    fun carNumber(): String

    fun carType(): Int

    suspend fun anchorTask()

}