package com.wb.logistics.ui.forcedtermination.domain

import com.wb.logistics.db.entity.attachedboxes.AttachedBoxEntity
import io.reactivex.Completable
import io.reactivex.Observable

interface ForcedTerminationInteractor {

    fun observeAttachedBoxes(dstOfficeId: Int): Observable<List<AttachedBoxEntity>>

    fun completeUnloading(dstOfficeId: Int, cause: String): Completable

}