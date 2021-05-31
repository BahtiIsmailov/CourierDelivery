package com.wb.logistics.ui.dcforcedtermination.domain

import com.wb.logistics.db.entity.dcunloadedboxes.DcNotUnloadedBoxEntity
import com.wb.logistics.db.entity.dcunloadedboxes.DcUnloadingScanBoxEntity
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface DcForcedTerminationInteractor {

    fun observeDcUnloadedBoxes(): Observable<DcUnloadingScanBoxEntity>

    fun completeUnloading(dstOfficeId: Int, cause: String): Completable

    fun notDcUnloadedBoxes(): Single<List<DcNotUnloadedBoxEntity>>

    fun switchScreen(): Completable

}