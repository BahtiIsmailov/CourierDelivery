package com.wb.logistics.ui.dcunloadingforcedtermination.domain

import com.wb.logistics.db.entity.dcunloadedboxes.DcNotUnloadedBoxEntity
import com.wb.logistics.network.monitor.NetworkState
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface DcForcedTerminationInteractor {

    fun observeNotDcUnloadedBoxes(): Observable<Int>

    fun notDcUnloadedBoxes(): Single<List<DcNotUnloadedBoxEntity>>

    fun switchScreenToClosed(data: String): Completable

    fun observeNetworkConnected(): Observable<NetworkState>

}