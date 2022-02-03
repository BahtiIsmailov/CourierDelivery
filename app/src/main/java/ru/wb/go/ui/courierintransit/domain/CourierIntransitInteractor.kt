package ru.wb.go.ui.courierintransit.domain

import io.reactivex.Observable
import io.reactivex.Single
import ru.wb.go.db.entity.courierlocal.LocalOfficeEntity
import ru.wb.go.db.entity.courierlocal.LocalOrderEntity
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.ui.couriermap.CourierMapAction
import ru.wb.go.ui.couriermap.CourierMapState
import ru.wb.go.ui.scanner.domain.ScannerState

interface CourierIntransitInteractor {

    fun observeNetworkConnected(): Observable<NetworkState>

    fun getOffices(): Observable<List<LocalOfficeEntity>>

    fun observeOfficeIdScanProcess(): Observable<CourierIntransitScanOfficeData>

    fun scannerAction(scannerAction: ScannerState)

    fun initOrderTimer(): Observable<Long>

    fun completeDelivery(): Single<CompleteDeliveryResult>

    fun clearLocalTaskData()

    fun observeMapAction(): Observable<CourierMapAction>

    fun mapState(state: CourierMapState)

    fun getOrder(): LocalOrderEntity
    fun getOrderId(): Single<String>
    fun getOfflineBoxes(): Int
}