package com.wb.logistics.ui.unloadingforcedtermination.domain

import com.wb.logistics.db.entity.attachedboxes.AttachedBoxEntity
import io.reactivex.Completable
import io.reactivex.Observable

interface ForcedTerminationInteractor {

    fun observeAttachedBoxes(dstOfficeId: Int): Observable<List<AttachedBoxEntity>>

    fun completeUnloading(data: String): Completable

}