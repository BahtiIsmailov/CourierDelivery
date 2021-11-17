package ru.wb.go.ui.dcunloadingforcedtermination.domain

import ru.wb.go.db.entity.dcunloadedboxes.DcNotUnloadedBoxEntity
import ru.wb.go.network.monitor.NetworkState
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface DcForcedTerminationInteractor {

    fun observeNotDcUnloadedBoxes(): Observable<Int>

    fun notDcUnloadedBoxes(): Single<List<DcNotUnloadedBoxEntity>>

    fun switchScreenToClosed(data: String): Completable

    fun observeNetworkConnected(): Observable<NetworkState>

}