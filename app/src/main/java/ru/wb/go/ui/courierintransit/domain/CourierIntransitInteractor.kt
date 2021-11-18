package ru.wb.go.ui.courierintransit.domain

import io.reactivex.Observable
import io.reactivex.Single
import ru.wb.go.db.entity.courierboxes.CourierIntransitGroupByOfficeEntity
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.ui.couriermap.CourierMapAction
import ru.wb.go.ui.couriermap.CourierMapState
import ru.wb.go.ui.scanner.domain.ScannerState

interface CourierIntransitInteractor {

    fun observeNetworkConnected(): Observable<NetworkState>

    fun observeBoxesGroupByOffice(): Observable<List<CourierIntransitGroupByOfficeEntity>>

    fun observeOfficeIdScanProcess(): Observable<CourierIntransitScanOfficeData>

    fun scannerAction(scannerAction: ScannerState)

    fun startTime(): Observable<Long>

    fun completeDelivery(): Single<CompleteDeliveryResult>

    fun observeMapAction(): Observable<CourierMapAction>

    fun mapState(state: CourierMapState)

    fun taskId(): Single<String>

}