package ru.wb.perevozka.ui.dcunloadingforcedtermination.domain

import ru.wb.perevozka.db.entity.dcunloadedboxes.DcNotUnloadedBoxEntity
import ru.wb.perevozka.network.monitor.NetworkState
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface DcForcedTerminationInteractor {

    fun observeNotDcUnloadedBoxes(): Observable<Int>

    fun notDcUnloadedBoxes(): Single<List<DcNotUnloadedBoxEntity>>

    fun switchScreenToClosed(data: String): Completable

    fun observeNetworkConnected(): Observable<NetworkState>

}